package cn.kduck.core.cache.handler;

import cn.kduck.core.cache.CacheExpiredHandler;
import cn.kduck.core.utils.ConversionUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.Cache;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MapCacheExpiredHandler implements CacheExpiredHandler {

    public static final int CLEAN_THRESHOLD = 5;

    private static final String EXPIRED_KEY_SUFFIX = "::EXPIRED";

    private final ObjectMapper objectMapper;

    private Object lock = new Object();

    public MapCacheExpiredHandler(ObjectMapper objectMapper){
        this.objectMapper = objectMapper;
    }

    @Override
    public void doExpired(Cache cache,Object key, Date expired) {
        synchronized (lock){
            String expiredJson = cache.get(getExpiredKey(cache), String.class);
            ConcurrentHashMap<Object,Long> expiredMap;
            if(expiredJson == null){
                expiredMap = new ConcurrentHashMap<>();
            }else{
                expiredMap = json2Bean(expiredJson, ConcurrentHashMap.class);
                if(expiredMap.size() >= CLEAN_THRESHOLD){
                    clearExpired(cache,expiredMap);
                }
            }
            expiredMap.put(key,expired.getTime());
            cache.put(getExpiredKey(cache),bean2Json(expiredMap));
        }
    }

    @Override
    public synchronized void clearExpired(Cache cache) {
        synchronized (lock) {
            String expiredJson = cache.get(getExpiredKey(cache), String.class);
            ConcurrentHashMap<Object, Long> expiredMap;
            if (expiredJson != null) {
                expiredMap = json2Bean(expiredJson, ConcurrentHashMap.class);
                if (!expiredMap.isEmpty()) {
                    clearExpired(cache,expiredMap);
                    cache.put(getExpiredKey(cache), bean2Json(expiredMap));
                }
            }
        }
    }

    @Override
    public boolean isExpired(Cache cache,Object key) {
        String expiredJson = cache.get(getExpiredKey(cache), String.class);
        Map<Object,Date> expiredMap = json2Bean(expiredJson,Map.class);
        if(expiredMap == null || !expiredMap.containsKey(key)) {
            return false;
        }
        Date expiredDate = ConversionUtils.convert(expiredMap.get(key),Date.class);
        return expiredDate.before(new Date());
    }

    private <T> T json2Bean(String json,Class<T> type){
        if(json == null){
            return null;
        }

        try {
            return objectMapper.readValue(json, type);
        } catch (IOException e) {
            throw new RuntimeException("缓存对象Json转换为类对象时错误：value->" + json + ",class->" + type,e);
        }
    }

    private String bean2Json(Object value){
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("缓存对象转换为Json时错误：" + value.getClass(),e);
        }
    }

    private void clearExpired(Cache cache,ConcurrentHashMap<Object, Long> expiredMap) {
        Iterator<Object> nameIterator = expiredMap.keySet().iterator();
        Date nowDate = new Date();
//        int size = expiredMap.size();
        while (nameIterator.hasNext()){
            Object keyName = nameIterator.next();
            Long expiredDate = ConversionUtils.convert(expiredMap.get(keyName),Long.class);
            if(new Date(expiredDate).before(nowDate)){
                expiredMap.remove(keyName);
                cache.evict(keyName);
                System.out.println("清理 KEY:" + keyName + ",FROM:" + cache.getName());
            }
        }
    }

    private String getExpiredKey(Cache cache){
        return cache.getName() + EXPIRED_KEY_SUFFIX;
    }
}
