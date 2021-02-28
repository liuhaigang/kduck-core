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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.filter.FormContentFilter;
import org.springframework.web.method.annotation.MapMethodProcessor;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;
//import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;
import java.util.List;

/**
 * LiuHG
 */
@Configuration
//@EnableSwagger2
@EnableSwagger2WebMvc
@EnableCaching
@Order(Ordered.HIGHEST_PRECEDENCE)
//@EnableWebMvc //不要启用，否则WebMvcConfigurer的接口类不会生效
public class WebConfiguration implements WebMvcConfigurer, ApplicationContextAware {

    private final List<ErrorViewResolver> errorViewResolvers;

//    @Autowired
//    private List<HttpMessageConverter<?>> messageConverters;

//    @Autowired
//    private BeanDefDepository beanDefDepository;

    @Autowired
    private RequestMappingHandlerAdapter handlerAdapter;

    @Autowired(required = false)
    private OperateInfoHandler operateInfoHandler;
    private ApplicationContext applicationContext;

    public WebConfiguration(ObjectProvider<List<ErrorViewResolver>> errorViewResolversProvider) {
        this.errorViewResolvers = errorViewResolversProvider.getIfAvailable();
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
//                .allowCredentials(true)
//                .allowedMethods("GET","POST");
//    }


    private CorsConfiguration buildConfig() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.setMaxAge(1800L);
//        corsConfiguration.addAllowedMethod("GET");
//        corsConfiguration.addAllowedMethod("POST");
//        corsConfiguration.addAllowedMethod("DELETE");
//        corsConfiguration.addAllowedMethod("PUT");
//        corsConfiguration.addAllowedMethod("OPTIONS");
        return corsConfiguration;
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", buildConfig());
        return new CorsFilter(source);
    }

//    @Override
//    public void afterPropertiesSet() throws Exception {
//        messageConverters.add(mappingJackson2HttpMessageConverter());
//
//        List<HandlerMethodArgumentResolver> argumentResolvers = handlerAdapter.getArgumentResolvers();
//        HandlerMethodArgumentResolver[] resolversArray = new HandlerMethodArgumentResolver[argumentResolvers.size() + 1];
//
//        int j =0;
//        for (int i = 0; i < argumentResolvers.size(); i++,j++) {
//            HandlerMethodArgumentResolver resolver = argumentResolvers.get(i);
//            if(resolver.getClass() == MapMethodProcessor.class){
//                resolversArray[j] = new ValueMapMethodArgumentResolver(messageConverters);
//                j++;
//            }
//            resolversArray[j] = resolver;
//
//        }
//        handlerAdapter.setArgumentResolvers(Arrays.asList(resolversArray));
//    }

    @Bean
    @ConditionalOnMissingBean(value=RestTemplate.class)
    public RestTemplate commonRestTemplate (){

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

        restTemplate.setMessageConverters(messageConverters);
        return restTemplate;
    }


//    @Bean
//    @ConditionalOnMissingBean(AutofillValue.class)
//    public AutofillValue autofillValue(){
//        return new StandardFieldAutofill();
//    }

    @Bean
    @ConditionalOnMissingBean(value = ErrorController.class, search = SearchStrategy.CURRENT)
    public ErrorController errorController(DefaultErrorAttributes errorAttributes) {
        return new GlobalErrorController(errorAttributes, this.errorViewResolvers);
    }


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
        this.applicationContext = applicationContext;
    }

}
