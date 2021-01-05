package cn.kduck.core.cache;

import cn.kduck.core.utils.SpringBeanUtils;

import java.util.Date;
import java.util.List;

public abstract class CacheHelper {

    private static CacheManagerWrapper cacheManager;

    private CacheHelper(){}

    public static void put(String cacheName,Object key,Object value,long expiredSeconds){
        CacheWrapper cache = getCache(cacheName);
        cache.put(key,value,expiredSeconds);
    }

    public static void put(String cacheName,Object key,Object value,Date expiredDate){
        put(cacheName,key,value,convertSeconds(expiredDate.getTime()));
    }

    public static void put(String cacheName,Object key,Object value){
        put(cacheName,key,value,-1);
    }

    public static void put(Object key,Object value){
        put(null,key,value,-1);
    }

    public static void put(Object key,Object value,long expiredSeconds){
        put(null,key,value,expiredSeconds);
    }

    public static void put(Object key, Object value, Date expiredDate){
        put(null,key,value,convertSeconds(expiredDate.getTime()));
    }

    public static <T> T getByCacheName(String cacheName,Object key,Class<T> cls){
        CacheWrapper cache = getCache(cacheName);
        return cache.get(key,cls);
    }

    public static Object getByCacheName(String cacheName,Object key){
        return getByCacheName(cacheName,key,Object.class);
    }

    public static Object get(Object key){
        return getByCacheName(null,key);
    }

    public static <T> T get(Object key,Class<T> cls){
        return getByCacheName(null,key,cls);
    }

    public static <T> List<T> getForList(Object key, Class<T> cls){
        return getForListByCacheName(null,key,cls);
    }

    public static <T> List<T> getForListByCacheName(String cacheName,Object key,Class<T> cls){
        CacheWrapper cache = getCache(cacheName);
        return cache.getForList(key,cls);
    }

    public static void evict(String cacheName,Object key){
        CacheWrapper cache = getCache(cacheName);
        cache.evict(key);
    }

    public static void evict(Object key){
        evict(null,key);
    }

    public static void clear(String cacheName){
        getCache(cacheName).clear();
    }

    private static long convertSeconds(long expiredTime){
        long now = System.currentTimeMillis();
        if(now >= expiredTime){
            throw new IllegalArgumentException("缓存过期时间不能小于当前时间");
        }
        return (expiredTime - now)/1000;
    }



    private static CacheWrapper getCache(String cacheName){
        if(cacheManager == null){
            synchronized (CacheHelper.class){
                cacheManager = SpringBeanUtils.getBean(CacheManagerWrapper.class);
            }
        }
        return cacheManager.getCache(cacheName);
    }
}
