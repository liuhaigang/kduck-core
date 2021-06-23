package cn.kduck.core.cache;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collection;

/**
 * @author LiuHG
 */
@Component
public class CacheManagerWrapper {

    private final static String DEFAULT_CACHE_NAME = "default";

    private final CacheManager cacheManager;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Value("${kduck.cache.cache-name-prefix:}")
    private String cacheNamePrefix;

    @Autowired
    public CacheManagerWrapper(CacheManager cacheManager){
        this.cacheManager = cacheManager;
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public CacheWrapper getCache(String name) {
        if(!StringUtils.hasText(name)){
            name = DEFAULT_CACHE_NAME;
        }

        if(cacheNamePrefix.length() > 0){
            name = cacheNamePrefix + name;
        }

        return new CacheWrapper(cacheManager.getCache(name),objectMapper);
    }

    public Collection<String> getCacheNames() {
        return cacheManager.getCacheNames();
    }

    public void clearExpired(String name) {
        CacheWrapper cache = getCache(name);
    }
}