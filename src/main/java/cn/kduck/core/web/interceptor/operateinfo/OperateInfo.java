package cn.kduck.core.web.interceptor.operateinfo;

/**
 *
 * @author LiuHG
 */
public class OperateInfo {

    public static final String CODE_SEPARATOR = "#";

    private final String code;
    private final String moduleName;
    private final String remoteHost;
    private final String operateName;
    private final String url;
    private final String method;
    private final String group;
    private final String version;

    public OperateInfo(String code, String url, String method, String remoteHost, String moduleName, String operateName,String group,String version){
        this.code = code;
        this.url = url;
        this.method = method;
        this.moduleName = moduleName;
        this.remoteHost = remoteHost;
        this.operateName = operateName;
        this.group = group;
        this.version = version;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getOperateName() {
        return operateName;
    }

    public String getMethod() {
        return method;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public String getUrl() {
        return url;
    }

    public String getCode() {
        return code;
    }

    public String getGroup() {
        return group;
    }

    public String getVersion() {
        return version;
    }
}
