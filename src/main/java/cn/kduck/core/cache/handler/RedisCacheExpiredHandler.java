package cn.kduck.core.cache.handler;

import cn.kduck.core.cache.CacheExpiredHandler;
import org.springframework.cache.Cache;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Date;

public class RedisCacheExpiredHandler implements CacheExpiredHandler {

    private final StringRedisTemplate redisTemplate;

    public RedisCacheExpiredHandler(StringRedisTemplate redisTemplate){
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void doExpired(Cache cache,Object key, Date expired) {
        redisTemplate.expireAt(cache.getName() + "::" + key.toString(),expired);
    }

    @Override
    public synchronized void clearExpired(Cache cache) {

    }

    @Override
    public boolean isExpired(Cache cache,Object key) {
        return false;
    }

}
