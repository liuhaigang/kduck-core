package cn.kduck.core.configuration;

import cn.kduck.core.cache.CacheExpiredHandler;
import cn.kduck.core.cache.handler.MapCacheExpiredHandler;
import cn.kduck.core.cache.handler.RedisCacheExpiredHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

public class CacheConfiguration {

    @Configuration
    @ConditionalOnClass(name="org.springframework.data.redis.connection.RedisConnectionFactory")
//    @ConditionalOnBean(org.springframework.data.redis.connection.RedisConnectionFactory.class)
    public static class RedisCacheExpiredHandlerConfiguration {

        private final Log logger = LogFactory.getLog(getClass());

        @Bean
        public CacheExpiredHandler redisCacheManagerWrapper(StringRedisTemplate redisTemplate){
            logger.info("CacheExpiredHandler:Redis");
            return new RedisCacheExpiredHandler(redisTemplate);
        }
    }

    @Configuration
    @ConditionalOnMissingClass("org.springframework.data.redis.connection.RedisConnectionFactory")
    public static class MapCacheExpiredHandlerConfiguration {

        private final Log logger = LogFactory.getLog(getClass());

        @Bean
        public CacheExpiredHandler mapCacheManagerWrapper(ObjectMapper objectMapper){
            logger.info("CacheExpiredHandler:Map");
            return new MapCacheExpiredHandler(objectMapper);
        }
    }

}
