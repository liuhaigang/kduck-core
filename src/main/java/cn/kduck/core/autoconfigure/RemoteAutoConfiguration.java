package cn.kduck.core.autoconfigure;

import cn.kduck.core.remote.scan.RemoteServiceScannerConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RemoteAutoConfiguration {

    @Bean
    public static RemoteServiceScannerConfigurer remoteServiceScannerConfigurer(){
        return new RemoteServiceScannerConfigurer();
    }
}
