package cn.kduck.core.cache.handler;

import cn.kduck.core.cache.CacheExpiredHandler;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.lang.Nullable;

import java.time.Duration;
import java.util.Date;

/**
 * 此类处理redis设置缓存的key过期时间的逻辑，不要使用StringRedisTemplate的expireAt方法设置过期时间，
 * 因为在RedisCache保存数据时使用自己的key、value的序列化逻辑，有可能和StringRedisTemplate中的不一致，导致put的key有可能无法获取到。
 * 同时将有指定过期时间的key，在放入缓存时同时指定过期时间。
 */
public class RedisCacheExpiredHandler implements CacheExpiredHandler {

    @Override
    public void doExpired(Cache cache,Object key, Object value, Date expired) {
//        redisTemplate.expireAt(cache.getName() + "::" + key.toString(),expired);
        RedisCache redisCache = (RedisCache)cache;
        RedisCache redisCacheExt = new RedisCache(redisCache.getName(),redisCache.getNativeCache(),redisCache.getCacheConfiguration()) {
            @Override
            public void put(Object key, @Nullable Object value) {

                Object cacheValue = preProcessCacheValue(value);

                if (!isAllowNullValues() && cacheValue == null) {

                    throw new IllegalArgumentException(String.format(
                            "Cache '%s' does not allow 'null' values. Avoid storing null via '@Cacheable(unless=\"#result == null\")' or configure RedisCache to allow 'null' via RedisCacheConfiguration.",
                            redisCache.getName()));
                }

                redisCache.getNativeCache().put(redisCache.getName(), serializeCacheKey(createCacheKey(key)), serializeCacheValue(cacheValue), Duration.ofMillis(expired.getTime() - System.currentTimeMillis()));
            }
        };
        redisCacheExt.put(key,value);

    }

    @Override
    public synchronized void clearExpired(Cache cache) {

    }

    @Override
    public boolean isExpired(Cache cache,Object key) {
        return false;
    }

}
