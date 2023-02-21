package cn.kduck.core.dao.definition.endpoint;


import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DefinitionEndpointConfiguration {


    @Bean
    @ConditionalOnMissingBean
    public DefinitionEndpoint definitionEndpoint() {
        return new DefinitionEndpoint();
    }

}
