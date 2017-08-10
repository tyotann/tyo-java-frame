package com.ihidea.component.cache.redis;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
        return (RedisTemplate<String, T>)redisTemplate;
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
            getRedisTemplate().opsForHash().putAll(key, (Map)value);
        } else if (value instanceof Set) {
            getRedisTemplate().opsForSet().add(key, (Set)value);
        } else {
            getRedisTemplate().opsForValue().set(key, value);
        }
        
        if (expire != null) {
            getRedisTemplate().expire(key, expire, TimeUnit.SECONDS);
        }
    }
    
    /**
     * 存set
     * 
     * @param key
     * @param value
     * @param expire
     */
    public static void putSet(final String key, final Object value, final Integer expire) {
        getRedisTemplate().opsForSet().add(key, value);
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
        
        return (T)getRedisTemplate().opsForValue().get(key);
    }
    
    @SuppressWarnings("unchecked")
    public static <T, clz> Map<T, clz> getMap(final String key, Class<? extends Object> clz, final Integer expire) {
        if (expire != null) {
            getRedisTemplate().expire(key, expire, TimeUnit.SECONDS);
        }
        return (Map<T, clz>)getRedisTemplate().opsForHash().entries(key);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> Set<T> getSet(final String key, Class<T> clz, final Integer expire) {
        if (expire != null) {
            getRedisTemplate().expire(key, expire, TimeUnit.SECONDS);
        }
        return (Set<T>)getRedisTemplate().opsForSet().members(key);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> List<T> find(final List<String> keys, Class<T> clz) {
        ValueOperations<String, T> valueOperations = (ValueOperations<String, T>)getRedisTemplate().opsForValue();
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
            
            return operations.range(key, PageLimitHolderFilter.getContext().getStartRowNo() - 1,
                PageLimitHolderFilter.getContext().getEndRowNo() - 1);
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
    
    /**
     * 是否存在key
     * 
     * @param key
     * @return
     */
    public static Boolean exits(String key) {
        return getRedisTemplate().hasKey(key);
    }
    
    public static Long getAtomicLong(String key) {
        return RedisClient.getRedisTemplate().boundValueOps(key).increment(0);
    }
    
    public static Long getAtomicLong(String key, String hashKey) {
        return RedisClient.getRedisTemplate().boundHashOps(key).increment(hashKey, 0);
    }
    
    public static boolean getLock(String lockId, long expire) {
        return getLock(lockId, expire, false);
    }
    
    /**
     * 互斥锁
     * 
     * @param alwaysSetExpire 每次都设置失效时间，可以防止因为没有release导致某个key锁死；或者可以在有需要每次延长失效时间的业务处理上使用
     */
    public static boolean getLock(String lockId, long expire, boolean alwaysSetExpire) {
        
        String key = "tmp:lock:" + lockId;
        
        boolean result = getRedisTemplate().boundValueOps(key).setIfAbsent(0);
        
        if (alwaysSetExpire || result) {
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
    
    /**
     * 批量获得hashmap中多个属性值
     *
     * @param redisTemplate
     * @param keys
     * @param hkeys
     * @return
     */
    public static <T> List<Object> hmMultiGet(RedisTemplate<T, ?> redisTemplate, final Collection<T> keys, final Collection<Object> hkeys) {
        
        boolean CurrentTransactionReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
        
        try {
            // Changes in version 1.7.3.RELEASE (2016-09-20)
            // Connection should be released when used with read-only
            // transactions.
            // 这里应该是spring-data-redis的一个bug，单execute中再次执行execute(hashOperations.entries会调用)，则会release2次Connection，第二次的时候就报错
            // 表现:会导致orderList异常,RedisUtils.hmMultiGet在service中调用的话就报错,在controller中不挂
            // https://jira.spring.io/browse/DATAREDIS-677
            TransactionSynchronizationManager.setCurrentTransactionReadOnly(false);
            
            return redisTemplate.executePipelined(new SessionCallback<Object>() {
                
                @SuppressWarnings("unchecked")
                @Override
                public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                    
                    HashOperations<T, Object, Object> hashOperations = (HashOperations<T, Object, Object>)operations.opsForHash();
                    
                    for (T key : keys) {
                        
                        if (CollectionUtils.isEmpty(hkeys)) {
                            hashOperations.entries(key);
                        } else {
                            hashOperations.multiGet(key, hkeys);
                        }
                    }
                    
                    return null;
                }
            });
            
        } finally {
            // 恢复原状
            TransactionSynchronizationManager.setCurrentTransactionReadOnly(CurrentTransactionReadOnly);
        }
    }
}
