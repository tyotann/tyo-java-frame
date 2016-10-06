package com.ihidea.core.support.cache;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.distribution.RMISynchronousCacheReplicator;
import net.sf.ehcache.event.CacheEventListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import com.ihidea.core.support.SpringContextLoader;

/**
 * <pre>
 * 此类一般用于静态方法的缓存
 * 调用spring cache接口
 * </pre>
 * @author TYOTANN
 */
@Component
public class CacheSupport {

	private static Log logger = LogFactory.getLog(CacheSupport.class);

	@PostConstruct
	public void init() {

	}

	/**
	 * 放置Cache
	 * @param cacheName 缓存名称,定义在spring-ehcache.xml中
	 * @param key 主键，利用方法名+主键方式生成Cache的KEY
	 * @param value 需要放置的值
	 */
	public static void put(String cacheName, String key, Object value) {
		getCache(cacheName).put(new Element(key, value));
	}

	/**
	 * 得到缓存的内容
	 * @param cacheName
	 * @param methodName
	 * @param key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T get(String cacheName, String key, Class<T> clz) {

		Element element = null;
		try {
			element = getCache(cacheName).get(key);
		} catch (Exception e) {
		}
		return element == null ? null : (T) element.getObjectValue();
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> get(String cacheName, Class<T> clz) {

		List<T> result = new ArrayList<T>();

		try {
			net.sf.ehcache.Cache cache = getCache(cacheName);

			for (Iterator<Object> i = cache.getKeys().iterator(); i.hasNext();) {
				result.add((T) cache.get(i.next()).getObjectValue());
			}
		} catch (Exception e) {
		}

		return result;
	}

	/**
	 * 移除缓存中内容
	 * @param cacheName
	 * @param methodName
	 * @param key
	 * @return
	 */
	public static void remove(String cacheName, String key) {
		getCache(cacheName).remove(key);
	}

	/**
	 * 清除缓存中所有内存
	 * @param cacheName
	 */
	public static void clear(String cacheName) {
		getCache(cacheName).removeAll();
	}

	/**
	 * 得到Spring中定义的Cache
	 * @param cacheName
	 * @return
	 */
	private static net.sf.ehcache.Cache getCache(String cacheName) {

		CacheManager cacheManager = SpringContextLoader.getBean(CacheManager.class);

		if (!cacheManager.cacheExists(cacheName)) {
			logger.info("没有找到名称为:" + cacheName + "的缓存配置,创建本地缓存!");
			cacheManager.addCache(cacheName);
		}

		return cacheManager.getCache(cacheName);
		// return SpringContextLoader.getBean(CacheManager.class).getCache(cacheName);
		// return SpringContextLoader.getBean(cacheName, net.sf.ehcache.Cache.class);
	}

	public static void addLocalCache(String cacheName) {
		addLocalCache(cacheName, null);
	}

	/**
	 * 添加缓存
	 * @param cacheName
	 * @param cluster
	 */
	public static void addLocalCache(String cacheName, Map<String, String> param) {

		CacheManager cacheManager = SpringContextLoader.getBean(CacheManager.class);

		if (!cacheManager.cacheExists(cacheName)) {

			cacheManager.addCache(cacheName);

			if (param != null) {

				// 设置cache的存活时间
				if (param.containsKey("timeToLiveSeconds")) {
					cacheManager.getCache(cacheName).getCacheConfiguration().setTimeToLiveSeconds(Long.valueOf(param.get("timeToLiveSeconds")));
				}
			}

			// TODO 集群模式
			// if (false) {
			// Properties properties = new Properties();
			// properties.setProperty("replicateAsynchronously", "false");
			// properties.setProperty("replicatePuts", "true");
			// properties.setProperty("replicateUpdates", "true");
			// properties.setProperty("replicateUpdatesViaCopy", "true");
			// properties.setProperty("replicateRemovals", "true");
			// getCache(cacheName).getCacheEventNotificationService().registerListener(
			// new RMICacheReplicatorFactory().createCacheEventListener(properties));
			// }
		}
	}

	/**
	 * 得到缓存列表一览
	 * @return
	 */
	public List<CacheEntity> getCacheList() {

		List<CacheEntity> result = new ArrayList<CacheEntity>();

		CacheManager cacheManager = SpringContextLoader.getBean(CacheManager.class);

		for (String cacheNames : cacheManager.getCacheNames()) {

			net.sf.ehcache.Cache cache = (net.sf.ehcache.Cache) cacheManager.getCache(cacheNames);

			CacheEntity entity = new CacheEntity();

			// 判断是否集群模式
			entity.setCluster(false);
			for (CacheEventListener cel : cache.getCacheEventNotificationService().getCacheEventListeners()) {
				if (cel instanceof RMISynchronousCacheReplicator) {
					entity.setCluster(true);
					break;
				}
			}

			entity.setName(cache.getName());

			entity.setSize(cache.getSize());

			entity.setMemoryStoreSize(cache.getMemoryStoreSize());

			// entity.setHits(cache.getStatistics().getCacheHits());
			//
			// entity.setMisses(cache.getStatistics().getCacheMisses());

			result.add(entity);
		}

		return result;
	}
}
