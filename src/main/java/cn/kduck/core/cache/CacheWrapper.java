package cn.kduck.core.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.Cache;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author LiuHG
 */
public class CacheWrapper implements Cache {

    private final Cache cache;
    private final ObjectMapper objectMapper;
    private final CacheExpiredHandler cacheExpiredHandler;

    public CacheWrapper(Cache cache, ObjectMapper objectMapper,CacheExpiredHandler cacheExpiredHandler){
        this.cache = cache;
        this.objectMapper = objectMapper;
        this.cacheExpiredHandler = cacheExpiredHandler;
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
    }


    @Override
    @Nullable
    public <T> T get(Object key, Callable<T> valueLoader) {
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
        Assert.notNull(key,"缓存key不能为null");
        Assert.notNull(key,"缓存value不能为null");
        //TODO 处理日期直接过期的情况
        String jsonValue = null;
        if(value != null){
            jsonValue = bean2Json(value);
        }
        cache.put(key, jsonValue);
        if(expired != null){
            cacheExpiredHandler.doExpired(cache,key,expired);
        }
    }

    public void clearExpired() {
        cacheExpiredHandler.clearExpired(cache);
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
        return cacheExpiredHandler.isExpired(cache,key);
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
