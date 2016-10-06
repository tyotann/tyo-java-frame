package com.ihidea.component.cache.redis;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import com.ihidea.core.support.SpringContextLoader;
import com.ihidea.core.support.pageLimit.PageLimit;
import com.ihidea.core.support.pageLimit.PageLimitHolderFilter;

@Lazy
@Component
public class RedisClient {

	private static RedisTemplate<String, ?> redisTemplate;

	public static RedisTemplate<String, Object> getRedisTemplate() {
		return getRedisTemplate(Object.class);
	}

	@SuppressWarnings("unchecked")
	public static <T> RedisTemplate<String, T> getRedisTemplate(Class<T> clz) {

		if (redisTemplate == null) {
			redisTemplate = SpringContextLoader.getBean(RedisTemplate.class);
		}
		return (RedisTemplate<String, T>) redisTemplate;
	}

	public static void put(final String key, final Object value) {
		put(key, value, null);
	}

	/**
	 * 移除key后重新放值
	 * 
	 * @param key
	 * @param value
	 */
	public void removePut(final String key, final Object value) {

		remove(key);

		put(key, value, null);
	}

	/**
	 * 塞入string
	 * 
	 * @param key
	 * @param value
	 * @param expire
	 */
	public static void put(final String key, final Object value, final Integer expire) {

		if (value instanceof Map) {
			getRedisTemplate().opsForHash().putAll(key, (Map) value);
		} else if (value instanceof Set) {
			getRedisTemplate().opsForSet().add(key, (Set) value);
		} else {
			getRedisTemplate().opsForValue().set(key, value);
		}

		if (expire != null) {
			getRedisTemplate().expire(key, expire, TimeUnit.SECONDS);
		}
	}

	public static <T> T get(final String key, Class<T> clz) {
		return get(key, clz, null);
	}

	@SuppressWarnings("unchecked")
	public static <T> T get(final String key, Class<T> clz, final Integer expire) {

		if (expire != null) {
			getRedisTemplate().expire(key, expire, TimeUnit.SECONDS);
		}

		return (T) getRedisTemplate().opsForValue().get(key);
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> find(final List<String> keys, Class<T> clz) {
		ValueOperations<String, T> valueOperations = (ValueOperations<String, T>) getRedisTemplate().opsForValue();
		return valueOperations.multiGet(keys);
	}

	/**
	 * 得到key数据结构未list的数据，带分页支持
	 */
	public static <T> List<T> findList(String key, Class<T> clz) {

		ListOperations<String, T> operations = RedisClient.getRedisTemplate(clz).opsForList();

		PageLimit pl = PageLimitHolderFilter.getContext();

		Long totalCnt = operations.size(key);

		if (pl != null && pl.limited() && !pl.isLimited()) {

			// 设置分页的总页数
			pl.setTotalCount(totalCnt.intValue());

			// 分页完成
			pl.setLimited(true);

			return operations.range(key, PageLimitHolderFilter.getContext().getStartRowNo() - 1, PageLimitHolderFilter.getContext()
					.getEndRowNo() - 1);
		} else {

			// 如果不分页则查出所有信息
			return operations.range(key, 0, totalCnt - 1);
		}

	}

	/**
	 * 删除某个key
	 * 
	 * @param key
	 */
	public static void remove(String key) {
		getRedisTemplate().delete(key);
	}

	/**
	 * 模糊删除key
	 * 
	 * @param key
	 */
	public static void clear(String key) {

		Set<String> keys = getRedisTemplate().keys(key);
		if (!keys.isEmpty()) {
			getRedisTemplate().delete(keys);
		}
	}

	public static Long getAtomicLong(String key) {
		return RedisClient.getRedisTemplate().boundValueOps(key).increment(0);
	}

	public static Long getAtomicLong(String key, String hashKey) {
		return RedisClient.getRedisTemplate().boundHashOps(key).increment(hashKey, 0);
	}

	/**
	 * 利用redis进行锁
	 */
	public static boolean getLock(String lockId, long expire) {

		String key = "tmp:lock:" + lockId;

		boolean result = getRedisTemplate().boundValueOps(key).setIfAbsent(0);

		if (result) {
			getRedisTemplate().expire(key, expire, TimeUnit.SECONDS);
		}

		return result;
	}

	/**
	 * 释放锁
	 */
	public static void releaseLock(String lockId) {
		getRedisTemplate().delete("tmp:lock:" + lockId);
	}
}
