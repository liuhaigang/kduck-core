package cn.kduck.core.remote.service;

import java.util.HashMap;
import java.util.Map;

public class RemoteServiceDepository {

    private static Map<String,Object> serviceDepository = new HashMap();


    private RemoteServiceDepository(){}

    static void addRemoteService(String serviceName,Object serviceObject){
        serviceDepository.put(serviceName,serviceObject);
    }

    public static Object getServiceObject(String serviceName){
        Object serviceObject = serviceDepository.get(serviceName);
        if(serviceObject == null){
            throw new RuntimeException("获取的远程服务对象不存在：" + serviceName);
        }
        return serviceObject;
    }

}
