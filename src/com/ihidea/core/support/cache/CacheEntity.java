package com.ihidea.core.support.cache;

public class CacheEntity {

	private String name;

	// 缓存中的对象个数
	private int size;

	// 缓存对象占用内存的大小(MB)
	private long memoryStoreSize;

	// 命中次数
	private long hits;

	// 错失次数
	private long misses;

	// 是否集群
	private boolean cluster;

	public boolean isCluster() {
		return cluster;
	}

	public void setCluster(boolean cluster) {
		this.cluster = cluster;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getMemoryStoreSize() {
		return memoryStoreSize;
	}

	public void setMemoryStoreSize(long memoryStoreSize) {
		this.memoryStoreSize = memoryStoreSize;
	}

	public long getHits() {
		return hits;
	}

	public void setHits(long hits) {
		this.hits = hits;
	}

	public long getMisses() {
		return misses;
	}

	public void setMisses(long misses) {
		this.misses = misses;
	}

}
