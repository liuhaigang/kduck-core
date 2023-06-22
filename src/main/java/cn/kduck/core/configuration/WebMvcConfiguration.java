package cn.kduck.core.configuration;

import cn.kduck.core.utils.ConversionUtils.DateConverter;
import cn.kduck.core.web.GlobalErrorController;
import cn.kduck.core.web.interceptor.DataSourceSwitchInterceptor;
import cn.kduck.core.web.interceptor.OperateIdentificationInterceptor;
import cn.kduck.core.web.interceptor.OperateInterceptor;
import cn.kduck.core.web.interceptor.ValidationInterceptor;
import cn.kduck.core.web.interceptor.operateinfo.OperateInfoHandler;
import cn.kduck.core.web.resolver.PageMethodArgumentResolver;
import cn.kduck.core.web.resolver.ValidErrorArgumentResolver;
import cn.kduck.core.web.resolver.ValueMapMethodArgumentResolver;
import cn.kduck.core.web.resource.ModelResourceLoader;
import cn.kduck.core.web.resource.ModelResourceProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.FormContentFilter;
import org.springframework.web.method.annotation.MapMethodProcessor;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

/**
 * LiuHG
 */
@Configuration
@EnableCaching
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnMissingClass("org.springframework.web.reactive.config.WebFluxConfigurer")
public class WebMvcConfiguration implements WebMvcConfigurer, ApplicationContextAware {

    @Autowired(required = false)
    private OperateInfoHandler operateInfoHandler;

    private ApplicationContext applicationContext;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new DataSourceSwitchInterceptor());
        registry.addInterceptor(new OperateIdentificationInterceptor(50));
        registry.addInterceptor(new ValidationInterceptor(applicationContext));
        if(operateInfoHandler != null){
            registry.addInterceptor(new OperateInterceptor(operateInfoHandler));
        }
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
//        argumentResolvers.batchAdd(new ValueMapMethodArgumentResolver());
//        argumentResolvers.add(new ValueBeanMethodArgumentResolver(beanDefDepository,messageConverters));
        argumentResolvers.add(new PageMethodArgumentResolver(500));
        argumentResolvers.add(new ValidErrorArgumentResolver());
    }

//    protected void addFormatters(FormatterRegistry registry) {
//        registry.addFormatter();
//    }


    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new DateConverter());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
    }

//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**")
//                .allowedOrigins("*")
//                .allowCredentials(false)
//                .allowedMethods("GET","POST");
//    }

    /**
     * 配置一个视图解析器，用来显示比如oauth2的用户授权页面，否则因为均是json请求，此视图解析器无需配置
     * @param registry
     */
    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        registry.viewResolver(new InternalResourceViewResolver());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        handlerAdapter = applicationContext.getBean(ObjectProvider<EventListener>);
        this.applicationContext = applicationContext;
    }



    @Configuration
    @EnableCaching
    public static class KduckWebConfiguration {

        private final List<ErrorViewResolver> errorViewResolvers;

        public KduckWebConfiguration(ObjectProvider<List<ErrorViewResolver>> errorViewResolversProvider) {
            this.errorViewResolvers = errorViewResolversProvider.getIfAvailable();
        }

        @Bean
        @ConditionalOnMissingBean(value = ErrorController.class)
        public ErrorController errorController(ErrorAttributes errorAttributes) {
            return new GlobalErrorController(errorAttributes, this.errorViewResolvers);
        }

        @Bean
        @ConditionalOnMissingBean(value = ErrorAttributes.class)
        public ErrorAttributes webErrorAttributes() {
            return new DefaultErrorAttributes();
        }

        @Bean
        @ConditionalOnBean(ModelResourceProcessor.class)
        @ConditionalOnProperty(prefix = "kduck.resource",value = "enabled",havingValue = "true",matchIfMissing = true)
        public ModelResourceLoader modelResourceLoader(ObjectProvider<ModelResourceProcessor> resourceProcessorList){
            return new ModelResourceLoader(resourceProcessorList);
        }

        //构造Json的转换器，否则会由JacksonHttpMessageConvertersConfiguration加入默认的转换器，导致转换的日期格式不是long
        @Bean
        public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(){
            Jackson2ObjectMapperBuilder builder = Jackson2ObjectMapperBuilder.json();
            ObjectMapper objectMapper = builder.build();
            objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            return new MappingJackson2HttpMessageConverter(objectMapper);
        }

        @Bean
        public FormContentFilter httpPutFormContentFilter() {
            return new FormContentFilter();
        }

        @Bean
        @ConditionalOnMissingBean(value=RestTemplate.class)
        public RestTemplate commonRestTemplate (@Autowired(required = false) RequestMappingHandlerAdapter handlerAdapter){

            RestTemplate restTemplate = new RestTemplate();

            List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
            //删除默认的Json转换器，后续会加入自定义的。
            for (HttpMessageConverter<?> messageConverter : messageConverters) {
                if(messageConverter instanceof MappingJackson2HttpMessageConverter){
                    messageConverters.remove(messageConverter);
                    break;
                }
            }
            messageConverters.add(mappingJackson2HttpMessageConverter());

            if(handlerAdapter!=null){
                processHandlerAdapter(handlerAdapter,messageConverters);
            }

            restTemplate.setMessageConverters(messageConverters);
            return restTemplate;
        }

        private void processHandlerAdapter(RequestMappingHandlerAdapter handlerAdapter,List<HttpMessageConverter<?>> messageConverters) {
            List<HandlerMethodArgumentResolver> argumentResolvers = handlerAdapter.getArgumentResolvers();
            HandlerMethodArgumentResolver[] resolversArray = new HandlerMethodArgumentResolver[argumentResolvers.size() + 1];

            int j =0;
            for (int i = 0; i < argumentResolvers.size(); i++,j++) {
                HandlerMethodArgumentResolver resolver = argumentResolvers.get(i);
                if(resolver.getClass() == MapMethodProcessor.class){
                    resolversArray[j] = new ValueMapMethodArgumentResolver(messageConverters);
                    j++;
                }
                resolversArray[j] = resolver;

            }
            handlerAdapter.setArgumentResolvers(Arrays.asList(resolversArray));
        }

//        @Override
//        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//            RequestMappingHandlerAdapter handlerAdapter = applicationContext.getBeanProvider(RequestMappingHandlerAdapter.class).getIfAvailable();
//
//            if(handlerAdapter == null){
//                return;
//            }
//
//            ObjectProvider<HttpMessageConverter> messageConverters = applicationContext.getBeanProvider(HttpMessageConverter.class);
//
//            List<HandlerMethodArgumentResolver> argumentResolvers = handlerAdapter.getArgumentResolvers();
//            HandlerMethodArgumentResolver[] resolversArray = new HandlerMethodArgumentResolver[argumentResolvers.size() + 1];
//
//            int j =0;
//            for (int i = 0; i < argumentResolvers.size(); i++,j++) {
//                HandlerMethodArgumentResolver resolver = argumentResolvers.get(i);
//                if(resolver.getClass() == MapMethodProcessor.class){
//                    resolversArray[j] = new ValueMapMethodArgumentResolver(messageConverters.stream().collect(Collectors.toList()));
//                    j++;
//                }
//                resolversArray[j] = resolver;
//
//            }
//            handlerAdapter.setArgumentResolvers(Arrays.asList(resolversArray));
//        }
    }
}
