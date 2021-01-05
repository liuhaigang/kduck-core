package cn.kduck.core.web.resource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import cn.kduck.core.service.ValueMap;

import java.util.Map;

/**
 *
 * @author LiuHG
 */
@JsonIgnoreProperties("operateName")
public class OperateValueMap extends ValueMap {

    /**操作名*/
    private static final String OPERATE_NAME = "operateName";
    /**操作编码*/
    private static final String OPERATE_CODE = "operateCode";
    /**操作路径*/
    private static final String OPERATE_PATH = "operatePath";
    /**所属组*/
    private static final String GROUP_CODE = "groupCode";
    /**请求方法*/
    private static final String METHOD = "method";


    public OperateValueMap() {
    }

    public OperateValueMap(Map<String, Object> map) {
        super(map);
    }

    /**
     * 设置 操作名
     *
     * @param operateName 操作名
     */
    public void setOperateName(String operateName) {
        super.setValue(OPERATE_NAME, operateName);
    }

    /**
     * 获取 操作名
     *
     * @return 操作名
     */
    public String getOperateName() {
        return super.getValueAsString(OPERATE_NAME);
    }

    /**
     * 设置 操作编码
     *
     * @param operateCode 操作编码
     */
    public void setOperateCode(String operateCode) {
        super.setValue(OPERATE_CODE, operateCode);
    }

    /**
     * 获取 操作编码
     *
     * @return 操作编码
     */
    public String getOperateCode() {
        return super.getValueAsString(OPERATE_CODE);
    }

    /**
     * 设置 操作路径
     *
     * @param operatePath 操作路径
     */
    public void setOperatePath(String operatePath) {
        super.setValue(OPERATE_PATH, operatePath);
    }

    /**
     * 获取 操作路径
     *
     * @return 操作路径
     */
    public String getOperatePath() {
        return super.getValueAsString(OPERATE_PATH);
    }

    /**
     * 设置 所属组
     *
     * @param groupCode 所属组
     */
    public void setGroupCode(String groupCode) {
        super.setValue(GROUP_CODE, groupCode);
    }

    /**
     * 获取 所属组
     *
     * @return 所属组
     */
    public String getGroupCode() {
        return super.getValueAsString(GROUP_CODE);
    }

    /**
     * 设置 请求方法
     *
     * @param method 请求方法
     */
    public void setMethod(String method) {
        super.setValue(METHOD, method);
    }

    /**
     * 获取 请求方法
     *
     * @return 请求方法
     */
    public String getMethod() {
        return super.getValueAsString(METHOD);
    }
}
