package cn.kduck.core.web.resource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import cn.kduck.core.service.ValueMap;

import java.util.List;
import java.util.Map;

/**
 * 注意，该对象在被序列化和反序列化时会忽略resourceName属性
 * @author LiuHG
 */
@JsonIgnoreProperties("resourceName")
public class ResourceValueMap extends ValueMap {

    /**资源名*/
    private static final String RESOURCE_NAME = "resourceName";
    /**资源编码*/
    private static final String RESOURCE_CODE = "resourceCode";
    /**资源操作*/
    private static final String OPERATE_LIST = "operateList";
    /**校验值*/
    private static final String MD5 = "md5";

    public ResourceValueMap() {
    }

    public ResourceValueMap(Map<String, Object> map) {
        super(map);
    }

    public void setResourceName(String resourceName) {
        super.setValue(RESOURCE_NAME, resourceName);
    }

    public String getResourceName() {
        return super.getValueAsString(RESOURCE_NAME);
    }

    public void setResourceCode(String resourceCode) {
        super.setValue(RESOURCE_CODE, resourceCode);
    }

    public String getResourceCode() {
        return super.getValueAsString(RESOURCE_CODE);
    }

    public void setOperateList(List<OperateValueMap> operateList) {
        super.setValue(OPERATE_LIST, operateList);
    }

    public List<OperateValueMap> getOperateList() {
        return super.getValueAsList(OPERATE_LIST);
    }

    public void setMd5(String md5) {
        super.setValue(MD5, md5);
    }

    public String getMd5() {
        return super.getValueAsString(MD5);
    }
}
