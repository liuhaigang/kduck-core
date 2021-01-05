package cn.kduck.core.web.swagger;

import com.fasterxml.classmate.TypeResolver;
import cn.kduck.core.dao.definition.BeanDefDepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import springfox.documentation.spring.web.DescriptionResolver;
import springfox.documentation.swagger.common.SwaggerPluginSupport;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

@Configuration
@ConditionalOnProperty(prefix = "kduck.swagger",value="enabled",havingValue = "true",matchIfMissing = true)
//@EnableSwagger2
@EnableSwagger2WebMvc
//@EnableOpenApi
//不要改名字回SwaggerConfiguration，因为在旧依赖包中已经有了这个名字的Bean，同名会出问题
public class SwaggerEnableConfiguration {


    @Bean
    @Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER + 5)
    public OperationValueMapReader operationValueMapReader(){
        return new OperationValueMapReader();
    }

    @Bean
    @Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER)
    public ParameterValueMapReader parameterValueMapReader(Environment environment){
        return new ParameterValueMapReader(new DescriptionResolver(environment));
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public OperationModelValueMapReader operationModelValueMapReader(BeanDefDepository beanDefDepository){
        return new OperationModelValueMapReader(new TypeResolver(),beanDefDepository);
    }

}
