package br.gov.serpro.scdp.util;

import java.util.Properties;

import br.gov.serpro.scdp.business.bo.UtilBO;
import br.gov.serpro.scdp.util.cache.ScdpInMemoryCache;

/**
 * @author p066251 - Vitor Alves Rocha
 * */
public class ConfiguracaoCacheUtil {
	
	private static final String nomeArquivoConfiguracao = "";
	private static ScdpInMemoryCache<String, Object> scdpCache;

	static {
		if(scdpCache == null){
			scdpCache = new ScdpInMemoryCache<String, Object>(1800000, 300000, 100);
		}
	}
	
	/**
	 * Busca no cache a configuração referente à chave.
	 * Caso não seja encontrada, busca no arquivo scdp.properties e insere no cache
	 * @param chave - chave que referencia a configuração 
	 * */
	public static Object get(String chave){
		Object valor = scdpCache.get(chave);
		
		if(valor == null){
			valor = buscarConfiguracaoArquivo(chave);
			scdpCache.put(chave, valor);
		}

		return valor;
	}
	
	/**
	 * Insere no cache a configuração referente à chave no arquivo scdp.properties 
	 * @param chave - chave que referencia a configuração
	 * */
	public static void put(String chave){
		Object valor = buscarConfiguracaoArquivo(chave);
		scdpCache.put(chave, valor);
	}
	
	private static Object buscarConfiguracaoArquivo(String chave){
		Properties propriedades = UtilBO.getFileProperties(nomeArquivoConfiguracao);
		Object valor = propriedades.getProperty(chave);
		return valor;
	}
}
