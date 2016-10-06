package com.ihidea.core.support.cache.memcached;

import java.util.Collection;

import net.spy.memcached.MemcachedClient;

import org.springframework.cache.Cache;
import org.springframework.cache.support.AbstractCacheManager;

public class MemcacheCacheManager extends AbstractCacheManager {

	private Collection<Cache> caches;

	private MemcachedClient client = null;

	public MemcacheCacheManager() {
	}

	public MemcacheCacheManager(MemcachedClient client) {
		setClient(client);
	}

	@Override
	protected Collection<? extends Cache> loadCaches() {
		return this.caches;
	}

	public void setCaches(Collection<Cache> caches) {
		this.caches = caches;
	}

	public void setClient(MemcachedClient client) {
		this.client = client;
		updateCaches();
	}

	public Cache getCache(String name) {

		checkState();

		Cache cache = super.getCache(name);
		if (cache == null) {
			cache = new MemcacheCache(name, client);
			addCache(cache);
		}
		return cache;
	}

	private void checkState() {
		if (client == null) {
			throw new IllegalStateException("MemcacheClient must not be null.");
		}
	}

	private void updateCaches() {
		if (caches != null) {
			for (Cache cache : caches) {
				if (cache instanceof MemcacheCache) {
					MemcacheCache memcacheCache = (MemcacheCache) cache;
					memcacheCache.setClient(client);
				}
			}
		}

	}
}
