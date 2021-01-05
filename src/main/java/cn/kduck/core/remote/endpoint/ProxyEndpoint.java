package cn.kduck.core.remote.endpoint;

import cn.kduck.core.remote.service.RemoteServiceProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebEndpoint(id = "proxy")
public class ProxyEndpoint {


    @Autowired(required = false)
    private List<RemoteServiceProxy> proxyList;

    @ReadOperation
    public Map<String,List<ProxyInfo>> proxyObject() {
        if(proxyList == null){
            return Collections.emptyMap();
        }

        Map<String,List<ProxyInfo>> proxyMap = new HashMap<>();

        List<ProxyInfo> proxyClientList = new ArrayList<>();
        List<ProxyInfo> proxyServerList = new ArrayList<>();
        for (RemoteServiceProxy serviceProxy : proxyList) {
            Object serviceObject = serviceProxy.getObject();
            String serviceName = serviceProxy.getServiceName();
            Class implClass = null;
            if(!serviceProxy.isClient()){
                implClass = serviceObject.getClass();
            }

            (serviceProxy.isClient() ? proxyClientList:proxyServerList).add(new ProxyInfo(serviceName,serviceProxy.getObjectType(),implClass));
        }
        if(!proxyServerList.isEmpty()){
            proxyMap.put("producer",proxyServerList);
        }
        if(!proxyClientList.isEmpty()){
            proxyMap.put("consumer",proxyClientList);
        }

        return proxyMap;
    }
}
