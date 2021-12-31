package cn.kduck.core.remote.service;

import cn.kduck.core.remote.annotation.ProxyService;
import cn.kduck.core.remote.exception.RemoteException;
import cn.kduck.core.remote.web.RemoteMethod;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RemoteServiceProxy implements FactoryBean,ApplicationContextAware, InitializingBean {

    private String serviceName;
    private final Class proxyClass;
    private Object serviceImpl;
    private ApplicationContext applicationContext;
    private boolean isClient = false;

    private ObjectMapper jsonMapper = new ObjectMapper();

    private String[] servicePath;

    private RestTemplate restTemplate;

    public RemoteServiceProxy(Class proxyClass){
        this.proxyClass = proxyClass;
    }

//    @Override
//    public void afterPropertiesSet() throws Exception {
//        ProxyService proxyService = AnnotationUtils.findAnnotation(proxyClass, ProxyService.class);
//        serviceName = proxyService.serviceName();
//        servicePath = proxyService.servicePaths();
//        try{
//            serviceImpl = applicationContext.getBean(proxyClass);
//            //将本地实现类放入缓存，供远程调用时使用
//            RemoteServiceDepository.addRemoteService(serviceName,serviceImpl);
//        }catch (Exception e){
//            //nothing
//        }
//    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object getObject() {
        ProxyService proxyService = AnnotationUtils.findAnnotation(proxyClass, ProxyService.class);
        serviceName = proxyService.serviceName();
        String clientPrefix = proxyService.clientPrefix();
        servicePath = proxyService.servicePaths();

        if(serviceImpl != null){
            RemoteServiceDepository.addRemoteService(serviceName,serviceImpl);
            return serviceImpl;
        }else{

            //由于spring bean对同一个接口加载顺序的问题，为了避免使用错误的Bean实例，因此判断如果Bean命名中不包含“#”，则认为是本地实现。
            //（DefaultBeanNameGenerator类负责生成的Bean的名称）
            String[] beanNamesForType = applicationContext.getBeanNamesForType(proxyClass);
            for (String beanName : beanNamesForType) {
                if(beanName.indexOf("#") == -1) {
                    serviceImpl = applicationContext.getBean(beanName);
                    RemoteServiceDepository.addRemoteService(serviceName,serviceImpl);
                    return serviceImpl;
                }
            }

            restTemplate = applicationContext.getBean(RestTemplate.class);

            Environment env = applicationContext.getEnvironment();
            String pathConfig = env.getProperty("kduck.proxy.service." + serviceName);
            //属性值可配置多个，说明该接口存在的位置不同。
            if(pathConfig == null){
                if(servicePath.length == 0) {
                    servicePath = new String[]{serviceName};
                }
            }else{
                servicePath = pathConfig.split("[,;]");
            }
            serviceImpl = new ProxyServiceImpl(clientPrefix,serviceName,servicePath,restTemplate,jsonMapper);
            isClient = true;
            return Proxy.newProxyInstance(proxyClass.getClassLoader(),new Class[]{proxyClass},(InvocationHandler)serviceImpl);
        }
    }

    @Override
    public Class<?> getObjectType() {
        return proxyClass;
    }

    public String getServiceName() {
        return serviceName;
    }

    public boolean isClient() {
        return isClient;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ProxyService proxyService = AnnotationUtils.findAnnotation(proxyClass, ProxyService.class);
        serviceName = proxyService.serviceName();
        RemoteServiceDepository.addRemoteServiceClass(serviceName,proxyClass);
    }

    public static class ProxyServiceImpl implements InvocationHandler {

        private final String serviceName;
        private final String[] servicePaths;
        private final RestTemplate restTemplate;
        private final ObjectMapper jsonMapper;
        private final String clientPrefix;

        private Map<String, Method> methodMap = new HashMap<>();

        /**
         *
         * @param serviceName 服务名
         * @param servicePaths 服务配置的路径
         * @param restTemplate 用于发起远程请求的RestTemplate
         * @param jsonMapper 用于序列化、范序列化请求参数和响应的json工具对象
         */
        public ProxyServiceImpl(String serviceName,String[] servicePaths, RestTemplate restTemplate, ObjectMapper jsonMapper){
            this("",serviceName,servicePaths,restTemplate,jsonMapper);
        }

        public ProxyServiceImpl(String clientPrefix,String serviceName,String[] servicePaths, RestTemplate restTemplate, ObjectMapper jsonMapper){
            this.serviceName = serviceName;
            this.servicePaths = servicePaths;
            this.restTemplate = restTemplate;
            this.jsonMapper = jsonMapper;
            this.clientPrefix = clientPrefix;
        }

        @Override
        public Object invoke(Object obj, Method method, Object[] args) throws Throwable {
            RemoteMethod remoteMethod = new RemoteMethod();
            remoteMethod.setMethodName(methodName(method));

            if("toString()".equals(remoteMethod.getMethodName())){
                return "来自" + serviceName + "服务的远程调用代理方法";
            }

            if(args != null){
                String[] paramStr = new String[args.length];
                for (int i = 0; i < paramStr.length; i++) {
                    paramStr[i] = jsonMapper.writeValueAsString(args[i]);
                }
                remoteMethod.setParams(paramStr);
            }

            ServletRequestAttributes servletRequestAttributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            MultiValueMap header = null;
            if(servletRequestAttributes != null){
                HttpServletRequest request = servletRequestAttributes.getRequest();

                //*************** 处理认证的Header ****************
                header = new LinkedMultiValueMap();
                String authorization = request.getHeader("Authorization");
                if(authorization != null) {
                    header.add("Authorization", authorization);
                }
                //***********************************************
            }

            HttpEntity httpEntity = new HttpEntity(remoteMethod,header);

            Class returnType = method.getReturnType();

            ResponseEntity responseEntity;

            //TODO 处理多个服务路径配置的情况
            String servicePath = servicePaths[0];
            if(!"".equals(clientPrefix)){
                for (String path : servicePaths) {
                    String[] split = path.split("[|]");
                    if(split.length != 2){
                        throw new RuntimeException("远程接口路径配置格式错误，对于带有前缀的远程接口配置，格式为：前缀1|路径1,前缀1|路径2。当前配置：" + path);
                    }
                    if(split[0].equals(clientPrefix)){
                        servicePath = split[1];
                    }
                }
            }

            servicePath = servicePath.endsWith("/") ? servicePath : servicePath + "/";
            servicePath += "proxy/" + serviceName;

            try{
                if("void".equals(returnType.getName())){
                    restTemplate.postForEntity(servicePath, httpEntity, String.class);
                    return null;
                } else {
                    if(List.class.isAssignableFrom(returnType)){
                        ParameterizedTypeReference<List> objectParameterizedTypeReference = ParameterizedTypeReference.forType(method.getGenericReturnType());
                        responseEntity = restTemplate.exchange(servicePath, HttpMethod.POST, httpEntity, objectParameterizedTypeReference);
                    }else{
                        responseEntity = restTemplate.exchange(servicePath, HttpMethod.POST, httpEntity, returnType);
                    }
                    return responseEntity.getBody();
                }
            }catch (Throwable e){
                throw new RemoteException("调用远程接口失败：" + servicePath,e);
            }
        }

        private String methodName(Method method){
            StringBuilder methodNameBuilder = new StringBuilder(method.getName());
            methodNameBuilder.append("(");
            Class<?>[] parameterTypes = method.getParameterTypes();
            for (int i = 0; i < parameterTypes.length; i++) {
                Class parameterType = parameterTypes[i];
                if(i  != 0){
                    methodNameBuilder.append(",");
                }
                methodNameBuilder.append(parameterType.getSimpleName());
            }
            methodNameBuilder.append(")");
            return methodNameBuilder.toString();
        }

    }
}
