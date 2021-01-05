package cn.kduck.core.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import cn.kduck.core.utils.ConversionUtils;
import org.springframework.cache.Cache;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author LiuHG
 */
public class CacheWrapper implements Cache {

    public static final int CLEAN_THRESHOLD = 5;

    private static final String EXPIRED_KEY_SUFFIX = "::EXPIRED";

    private final Cache cache;
    private final ObjectMapper objectMapper;

    public CacheWrapper(Cache cache, ObjectMapper objectMapper){
        this.cache = cache;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getName() {
        return cache.getName();
    }

    @Override
    public Object getNativeCache() {
        return cache.getNativeCache();
    }

    @Override
    @Nullable
    public ValueWrapper get(Object key) {
        return cache.get(key);
    }


    private String getExpiredKey(){
        return getName() + EXPIRED_KEY_SUFFIX;
    }

    @Override
    @Nullable
    public <T> T get(Object key, Class<T> type) {
        if(isExpired(key)){
            clearByKey(key);
            return null;
        }
        String jsonValue = cache.get(key, String.class);
        return json2Bean(jsonValue,type);
    }


    public <T> List<T> getForList(Object key, Class<T> type) {
        if(isExpired(key)){
            clearByKey(key);
            return null;
        }
        String jsonValue = cache.get(key, String.class);
        return json2ListBean(jsonValue,type);
    }

    private void clearByKey(Object key) {
        cache.evict(key);
        String json = cache.get(getExpiredKey(), String.class);
        Map expiredMap = json2Bean(json, Map.class);
        expiredMap.remove(key);
        cache.put(getExpiredKey(),bean2Json(expiredMap));
    }


    @Override
    @Nullable
    public <T> T get(Object key, Callable<T> valueLoader) {
//        return cache.get(key, valueLoader);
        throw new UnsupportedOperationException("不支持此方法");
    }

    @Override
    public void put(Object key, Object value) {
        this.put(key, value,null);
    }

    public void put(Object key, Object value,long expired) {
        if(expired > 0){
            long expiredTimeMillis = System.currentTimeMillis() + expired * 1000;
            this.put(key, value,new Date(expiredTimeMillis));
        }else{
            this.put(key, value,null);
        }
    }

    public void put(Object key, Object value,Date expired) {
        //TODO 处理日期直接过期的情况
        String jsonValue = null;
        if(value != null){
            jsonValue = bean2Json(value);
        }
        cache.put(key, jsonValue);
        if(expired != null){
            String expiredJson = cache.get(getExpiredKey(), String.class);
            ConcurrentHashMap<Object,Date> expiredMap;
            if(expiredJson == null){
                expiredMap = new ConcurrentHashMap<>();
            }else{
                expiredMap = json2Bean(expiredJson, ConcurrentHashMap.class);
                if(expiredMap.size() >= CLEAN_THRESHOLD){
                    clearExpired(expiredMap);
                }
            }
            expiredMap.put(key,expired);
            cache.put(getExpiredKey(),bean2Json(expiredMap));
        }
    }

    public void clearExpired() {
        String expiredJson = cache.get(getExpiredKey(), String.class);
        ConcurrentHashMap<Object,Date> expiredMap;
        if(expiredJson != null){
            expiredMap = json2Bean(expiredJson, ConcurrentHashMap.class);
            if(!expiredMap.isEmpty()){
                clearExpired(expiredMap);
                cache.put(getExpiredKey(),bean2Json(expiredMap));
            }
        }
    }

    private void clearExpired(ConcurrentHashMap<Object, Date> expiredMap) {
        Iterator<Object> nameIterator = expiredMap.keySet().iterator();
        Date nowDate = new Date();
//        int size = expiredMap.size();
        while (nameIterator.hasNext()){
            Object keyName = nameIterator.next();
            Date expiredDate = ConversionUtils.convert(expiredMap.get(keyName),Date.class);
            if(expiredDate.before(nowDate)){
                expiredMap.remove(keyName);
                evict(keyName);
            }
        }
    }

    private String bean2Json(Object value){
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("缓存对象转换为Json时错误：" + value.getClass(),e);
        }
    }

    private <T> List<T> json2ListBean(String json,Class<T> type){
        if(json == null){
            return null;
        }
        JavaType listType = objectMapper.getTypeFactory().constructParametricType(
                List.class, type);
        try {
            return objectMapper.readValue(json, listType);
        } catch (IOException e) {
            throw new RuntimeException("缓存对象Json转换为类对象时错误：value->" + json + ",class->" + type,e);
        }
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

    private boolean isExpired(Object key) {
        String expiredJson = cache.get(getExpiredKey(), String.class);
        Map<Object,Date> expiredMap = json2Bean(expiredJson,Map.class);
        if(expiredMap == null || !expiredMap.containsKey(key)) {
            return false;
        }
        Date expiredDate = ConversionUtils.convert(expiredMap.get(key),Date.class);
        return expiredDate.before(new Date());
    }

    @Override
    @Nullable
    public ValueWrapper putIfAbsent(Object key, Object value) {
        return cache.putIfAbsent(key, value);
    }

    @Override
    public void evict(Object key) {
        cache.evict(key);
//        clearExpired();//此行会导致死循环堆栈溢出
    }

    @Override
    public void clear() {
        cache.clear();
    }
}
