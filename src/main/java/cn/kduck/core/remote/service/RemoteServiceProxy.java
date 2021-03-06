package cn.kduck.core.remote.service;

import cn.kduck.core.remote.annotation.ProxyService;
import cn.kduck.core.remote.exception.RemoteException;
import cn.kduck.core.remote.web.RemoteMethod;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
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

public class RemoteServiceProxy implements FactoryBean,ApplicationContextAware {

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
//            //?????????????????????????????????????????????????????????
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
        servicePath = proxyService.servicePaths();

        if(serviceImpl != null){
            RemoteServiceDepository.addRemoteService(serviceName,serviceImpl);
            return serviceImpl;
        }else{

            //??????spring bean?????????????????????????????????????????????????????????????????????Bean???????????????????????????Bean?????????????????????#?????????????????????????????????
            //???DefaultBeanNameGenerator??????????????????Bean????????????
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
            //??????????????????????????????????????????????????????????????????
            if(pathConfig == null){
                if(servicePath.length == 0) {
                    servicePath = new String[]{serviceName};
                }
            }else{
                servicePath = pathConfig.split("[,;]");
            }
            serviceImpl = new ProxyServiceImpl(serviceName,servicePath,restTemplate,jsonMapper);
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

    public static class ProxyServiceImpl implements InvocationHandler {

        private final String serviceName;
        private final String[] servicePaths;
        private final RestTemplate restTemplate;
        private final ObjectMapper jsonMapper;

        private Map<String, Method> methodMap = new HashMap<>();

        /**
         *
         * @param serviceName ?????????
         * @param servicePaths ?????????????????????
         * @param restTemplate ???????????????????????????RestTemplate
         * @param jsonMapper ??????????????????????????????????????????????????????json????????????
         */
        public ProxyServiceImpl(String serviceName,String[] servicePaths, RestTemplate restTemplate, ObjectMapper jsonMapper){
            this.serviceName = serviceName;
            this.servicePaths = servicePaths;
            this.restTemplate = restTemplate;
            this.jsonMapper = jsonMapper;
        }

        @Override
        public Object invoke(Object obj, Method method, Object[] args) throws Throwable {
            RemoteMethod remoteMethod = new RemoteMethod();
            remoteMethod.setMethodName(methodName(method));

            if("toString()".equals(remoteMethod.getMethodName())){
                return "??????" + serviceName + "?????????????????????????????????";
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
            HttpServletRequest request = servletRequestAttributes.getRequest();

            //*************** ???????????????Header ****************
            MultiValueMap header = new LinkedMultiValueMap();
            String authorization = request.getHeader("Authorization");
            if(authorization != null) {
                header.add("Authorization", authorization);
            }
            //***********************************************

            HttpEntity httpEntity = new HttpEntity(remoteMethod,header);

            Class returnType = method.getReturnType();

            ResponseEntity responseEntity;

            //TODO ???????????????????????????????????????
            String servicePath = servicePaths[0];
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
                throw new RemoteException("???????????????????????????" + servicePath,e);
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
