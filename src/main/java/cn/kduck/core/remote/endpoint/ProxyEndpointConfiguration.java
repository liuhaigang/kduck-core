package cn.kduck.core.remote.endpoint;


import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
//@ConditionalOnEnabledEndpoint(endpoint = ProxyEndpoint.class)
public class ProxyEndpointConfiguration {


    @Bean
    @ConditionalOnMissingBean
    public ProxyEndpoint proxyEndpoint() {
        return new ProxyEndpoint();
    }

}
