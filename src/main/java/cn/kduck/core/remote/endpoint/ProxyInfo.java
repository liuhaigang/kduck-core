package cn.kduck.core.remote.endpoint;

public class ProxyInfo {

    private final String serviceName;
    private final Class proxyClass;
    private final Class implClass;

    public ProxyInfo(String serviceName, Class proxyClass, Class implClass){
        this.serviceName = serviceName;
        this.proxyClass = proxyClass;
        this.implClass = implClass;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getProxyClass() {
        return proxyClass.getName();
    }

    public String getImplClass() {
        if(implClass == null){
            return null;
        }
        return implClass.getName();
    }
}
