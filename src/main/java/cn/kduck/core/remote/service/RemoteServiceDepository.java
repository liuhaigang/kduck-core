package cn.kduck.core.remote.service;

import cn.kduck.core.remote.annotation.ProxyService;
import cn.kduck.core.utils.SpringBeanUtils;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.HashMap;
import java.util.Map;

public class RemoteServiceDepository {

    private static Map<String,Object> serviceDepository = new HashMap();
    private static Map<String,Class> serviceClassDepository = new HashMap();

    private RemoteServiceDepository(){}

    static void addRemoteService(String serviceName,Object serviceObject){
        serviceDepository.put(serviceName,serviceObject);
    }

    static void addRemoteServiceClass(Class proxyClass){
        ProxyService proxyService = AnnotationUtils.findAnnotation(proxyClass, ProxyService.class);
        String serviceName = proxyService.serviceName();
        serviceClassDepository.put(serviceName,proxyClass);
    }

    public static Object getServiceObject(String serviceName){
        Object serviceObject = serviceDepository.get(serviceName);

        if(serviceObject == null){
            Class serviceClass = serviceClassDepository.get(serviceName);
            serviceObject = SpringBeanUtils.getBean(serviceClass);
            serviceDepository.put(serviceName,serviceObject);
        }

        if(serviceObject == null){
            throw new RuntimeException("获取的远程服务对象不存在：" + serviceName);
        }
        return serviceObject;
    }

}
