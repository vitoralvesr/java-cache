package br.gov.serpro.scdp.testsuite.util;

import br.gov.serpro.scdp.util.cache.ScdpInMemoryCache;

public class TestScdpInMemoryCache {
	public static void main(String[] args) throws InterruptedException {
		TestScdpInMemoryCache scdpCache = new TestScdpInMemoryCache();

		System.out.println("\n\n==========Test1: scdpTestAddRemoveObjects ==========");
		scdpCache.scdpTestAddRemoveObjects();
//		System.out.println("\n\n==========Test2: scdpTestExpiredCacheObjects ==========");
//		scdpCache.scdpTestExpiredCacheObjects();
//		System.out.println("\n\n==========Test3: scdpTestObjectsCleanupTime ==========");
//		scdpCache.scdpTestObjectsCleanupTime();
	}

	private void scdpTestAddRemoveObjects() throws InterruptedException {
		// Test with scdpTimeToLive = 200 seconds
		// scdpTimerInterval = 500 seconds
		// maxItems = 6
		ScdpInMemoryCache<String, String> cache = new ScdpInMemoryCache<String, String>(1000, 500, 6);

		Thread.sleep(1000);
		
		cache.put("eBay", "eBay");
		cache.put("Paypal", "Paypal");
		cache.put("Google", "Google");
		cache.put("Microsoft", "Microsoft");
		cache.put("IBM", "IBM");
		cache.put("Facebook", "Facebook");

		System.out.println("6 Cache Object Added.. cache.size(): " + cache.size());
		cache.remove("IBM");
		System.out.println("One object removed.. cache.size(): " + cache.size());

		cache.put("Twitter", "Twitter");
		cache.put("SAP", "SAP");
		System.out.println("Two objects Added but reached maxItems.. cache.size(): " + cache.size());
	}

	private void scdpTestExpiredCacheObjects() throws InterruptedException {
		// Test with scdpTimeToLive = 1 second
		// scdpTimerInterval = 1 second
		// maxItems = 10
		ScdpInMemoryCache<String, String> cache = new ScdpInMemoryCache<String, String>(1, 1, 10);

		cache.put("eBay", "eBay");
		cache.put("Paypal", "Paypal");
		// Adding 3 seconds sleep.. Both above objects will be removed from
		// Cache because of timeToLiveInSeconds value
		Thread.sleep(3000);

		System.out.println("Two objects are added but reached timeToLive. cache.size(): " + cache.size());
	}

	private void scdpTestObjectsCleanupTime() throws InterruptedException {
		int size = 500000;

		// Test with timeToLiveInSeconds = 100 seconds
		// timerIntervalInSeconds = 100 seconds
		// maxItems = 500000

		ScdpInMemoryCache<String, String> cache = new ScdpInMemoryCache<String, String>(100, 100, 500000);

		for (int i = 0; i < size; i++) {
			String value = Integer.toString(i);
			cache.put(value, value);
		}

		Thread.sleep(200);

		long start = System.currentTimeMillis();
		cache.cleanup();
		double finish = (double) (System.currentTimeMillis() - start) / 1000.0;

		System.out.println("Cleanup times for " + size + " objects are " + finish + " s");
	}
}
