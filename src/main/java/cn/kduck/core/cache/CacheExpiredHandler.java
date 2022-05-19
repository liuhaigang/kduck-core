package cn.kduck.core.cache;

import org.springframework.cache.Cache;

import java.util.Date;

/**
 * 缓存过期处理器
 */
public interface CacheExpiredHandler {

    void doExpired(Cache cache,Object key, Date expired);

    void clearExpired(Cache cache);

    boolean isExpired(Cache cache,Object key);

}
