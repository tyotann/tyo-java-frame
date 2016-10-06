package com.ihidea.core.support.cache.memcached;

import net.spy.memcached.MemcachedClient;

import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.util.Assert;

public class MemcacheCache implements Cache {

	private MemcachedClient client;

	private String name;

	public MemcacheCache(String name, MemcachedClient client) {
		Assert.notNull(client, "Memcache client must not be null");
		this.client = client;
		this.name = name;
	}

	public void setClient(MemcachedClient client) {
		this.client = client;
	}

	public String getName() {
		return this.name;
	}

	public Object getNativeCache() {
		return this.client;
	}

	public ValueWrapper get(Object key) {
		Object value = this.client.get(objectToString(key));
		return (value != null ? new SimpleValueWrapper(value) : null);
	}

	// 默认为30天失效
	public void put(Object key, Object value) {
		this.client.set(objectToString(key), 3600, value);
	}

	public void evict(Object key) {
		this.client.delete(objectToString(key));
	}

	// TODO memcache没有模糊查询key
	public void clear() {
		// this.client.removeAll();
		// TODO memcache没有模糊查询key
	}

	private String objectToString(Object object) {
		if (object == null) {
			return null;
		} else if (object instanceof String) {
			return name + "." + (String) object;
		} else {
			return name + "." + object.toString();
		}
	}
}
