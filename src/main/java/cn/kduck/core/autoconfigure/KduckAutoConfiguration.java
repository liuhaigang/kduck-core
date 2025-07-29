package cn.kduck.core.autoconfigure;

import cn.kduck.core.KduckProperties;
import cn.kduck.core.configuration.DaoConfiguration;
import cn.kduck.core.configuration.WebMvcConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableCaching
@ComponentScan("cn.kduck")
@EnableConfigurationProperties({ KduckProperties.class })
@Import({DaoConfiguration.class, WebMvcConfiguration.class})
public class KduckAutoConfiguration {

}
