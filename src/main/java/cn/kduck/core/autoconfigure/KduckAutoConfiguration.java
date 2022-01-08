package cn.kduck.core.autoconfigure;

import cn.kduck.core.KduckProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("cn.kduck")
@EnableConfigurationProperties({ KduckProperties.class })
public class KduckAutoConfiguration {

}
