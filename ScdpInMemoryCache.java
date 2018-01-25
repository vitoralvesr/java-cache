import java.util.ArrayList;

import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.map.LRUMap;

/**
 * @author p066251 - Vitor Alves Rocha
 * */
public class ScdpInMemoryCache<K, T> {
	//tempo de vida do cache
	private long tempoDeVidaCache;
	//coleção de objetos com tamanho definido
	//ao inserir um item, o item menos acessado da lista é removido (Least Recently Used)
	private LRUMap scdpCacheMap;

	protected class ScdpCacheObject {
		//horario em milisegundos para controle do último item inserido no cache
		public long horarioDeCriacao = System.currentTimeMillis();
		//objeto a ser inserido no cache
		public T valor;

		protected ScdpCacheObject(T valor) {
			this.valor = valor;
		}
	}

	/**
	 * @param scdpTempoDeVidaLRUMap - tempo de vida do cache
	 * @param scdpTimerInterval - intervalo para rotina de limpeza do cache
	 * @param maxItens - número máximo de itens a serem inseridos no cache
	 * */
	public ScdpInMemoryCache(long scdpTempoDeVidaLRUMap, final long scdpTimerInterval, int maxItens) {
		this.tempoDeVidaCache = scdpTempoDeVidaLRUMap;

		scdpCacheMap = new LRUMap(maxItens);

		//rotina de verificação da existencia de itens a serem removidos do cache
		if (tempoDeVidaCache > 0 && scdpTimerInterval > 0) {
			Thread t = new Thread(new Runnable() {
				public void run() {
					//se a aplicação estiver em execução, continua a iteração
					while (true) {
						try {
							//tempo de intervalo para acionar o cleanUp()
							Thread.sleep(scdpTimerInterval);
						} catch (InterruptedException ex) {
							//TODO: inserir log
						}

						cleanup();
					}
				}
			});

			//define que a thread será finalizada pela JVM caso a main thread (sistema) seja finalizada
			t.setDaemon(true);
			t.start();
		}
	}

	/**
	 * Insere um item no cache
	 * @param chave - chave de identificação do item no cache
	 * @param valor - valor a ser inserido no cache
	 * */
	public void put(K chave, T valor) {
		//sincronizado para não haver concorrência entre processos, garantindo exclusividade no acesso ao LRUMap
		synchronized (scdpCacheMap) {
			scdpCacheMap.put(chave, new ScdpCacheObject(valor));
		}
	}

	/**
	 * Busca um item específico no cache a partir de uma chave
	 * @param chave - chave de identificação do item no cache
	 * */
	@SuppressWarnings("unchecked")
	public T get(K chave) {
		//sincronizado para não haver concorrência entre processos, garantindo exclusividade no acesso ao LRUMap
		synchronized (scdpCacheMap) {
			ScdpCacheObject c = (ScdpCacheObject) scdpCacheMap.get(chave);

			if (c == null)
				return null;
			else {
				c.horarioDeCriacao = System.currentTimeMillis();
				return c.valor;
			}
		}
	}

	/**
	 * Remove um item específico do cache a partir de uma chave
	 * @param chave - chave de identificação do item no cache
	 * */
	public void remove(K chave) {
		//sincronizado para não haver concorrência entre processos, garantindo exclusividade no acesso ao LRUMap
		synchronized (scdpCacheMap) {
			scdpCacheMap.remove(chave);
		}
	}

	/**
	 * Retorna o tamanho da LRUMap
	 * */
	public int size() {
		//sincronizado para não haver concorrência entre processos, garantindo exclusividade no acesso ao LRUMap
		synchronized (scdpCacheMap) {
			return scdpCacheMap.size();
		}
	}

	/**
	 * Faz a limpeza do cache (GarbageCollector).
	 * Varre a LRUMap e verifica o tempo de vida de cada item. Se já tiver acabado, remove o item.
	 * */
	@SuppressWarnings("unchecked")
	public void cleanup() {
		long now = System.currentTimeMillis();
		
		//lista de chaves de itens a serem deletados
		ArrayList<K> listaChaveDeletar = null;

		//sincronizado para não haver concorrência entre processos, garantindo exclusividade no acesso ao LRUMap
		synchronized (scdpCacheMap) {
			MapIterator iterator = scdpCacheMap.mapIterator();

			listaChaveDeletar = new ArrayList<K>((scdpCacheMap.size() / 2) + 1);
			K chave = null;
			ScdpCacheObject item = null;

			while (iterator.hasNext()) {
				chave = (K) iterator.next();
				item = (ScdpCacheObject) iterator.getValue();

				//se o tempo de vida do item tiver acabado ele é adicionado à lista de itens a serem removidos
				if (item != null && (now > (tempoDeVidaCache + item.horarioDeCriacao))) {
					listaChaveDeletar.add(chave);
				}
			}
		}

		for (K chave : listaChaveDeletar) {
			//sincronizado para não haver concorrência entre processos, garantindo exclusividade no acesso ao LRUMap
			synchronized (scdpCacheMap) {
				scdpCacheMap.remove(chave);
			}

			//assinala para o sistema que essa thread já finalizou o processo (perde a prioridade)
			Thread.yield();
		}
	}
}
