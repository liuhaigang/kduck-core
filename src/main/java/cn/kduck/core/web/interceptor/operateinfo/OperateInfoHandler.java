package cn.kduck.core.web.interceptor.operateinfo;

import cn.kduck.core.web.interceptor.OperateInterceptor;
import cn.kduck.core.service.DefaultService;

import java.util.List;
import java.util.Map;

/**
 * 审计日志处理接口，由于记录的是整体操作，需要{@link OperateInterceptor OperateInterceptor}拦截器的配合，
 * 因此要求必须经过Controller，即如果直接基于Service测试，审计日志记录不会生效。
 * @author LiuHG
 */
public interface OperateInfoHandler {

    /**
     * 审计日志记录方法，需要注意：
     * <lu>
     *     <li>对于operateObjectList参数，如果调用{@link DefaultService#executeUpdate(String, Map)} 方法，operateObjectList不会记录操作对象的。</li>
     *     <li>对于查询方法operateObjectList不会包含操作对象、返回结果等信息。</li>
     * </lu>
     * @param success 当前请求是否成功，即是否无异常调用
     * @param operateInfo 审计信息，包含请求的资源及操作信息
     * @param operateObjects 本次操作所涉及的数据对象信息，即添加、修改、删除的数据内容。
     */
    void doHandle(boolean success, OperateInfo operateInfo, List<OperateObject> operateObjects);
}
