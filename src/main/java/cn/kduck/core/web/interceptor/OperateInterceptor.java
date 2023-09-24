package cn.kduck.core.web.interceptor;

import cn.kduck.core.web.annotation.ModelOperate;
import cn.kduck.core.web.annotation.ModelResource;
import cn.kduck.core.web.interceptor.OperateIdentificationInterceptor.OidHolder;
import cn.kduck.core.web.interceptor.OperateIdentificationInterceptor.OperateIdentification;
import cn.kduck.core.web.interceptor.operateinfo.OperateInfo;
import cn.kduck.core.web.interceptor.operateinfo.OperateInfoHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;

/**
 * @author LiuHG
 */
public class OperateInterceptor implements HandlerInterceptor {

    private final OperateInfoHandler operateInfoHandler;

    private ThreadLocal<OperateInfo> auditInfoThreadLocal = new ThreadLocal();

    public OperateInterceptor(OperateInfoHandler operateInfoHandler){
        this.operateInfoHandler = operateInfoHandler;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(operateInfoHandler == null) return true;

        if(handler instanceof HandlerMethod){
            HandlerMethod handlerMethod = ((HandlerMethod)handler);
            Class<?> typeClass = handlerMethod.getBeanType();
            Method method = handlerMethod.getMethod();
            ModelResource modelResource = typeClass.getAnnotation(ModelResource.class);
            ModelOperate modelOperate = handlerMethod.getMethodAnnotation(ModelOperate.class);
            if (modelResource != null && modelOperate != null){
                //得到模块名称
                String moduleName = modelResource.value();
                if(ObjectUtils.isEmpty(moduleName)) {
                    moduleName = modelResource.code();
                }
                if(ObjectUtils.isEmpty(moduleName)) {
                    moduleName = typeClass.getSimpleName();
                }

                //得到操作名称
                String optName = modelOperate.name();
                if(ObjectUtils.isEmpty(optName)) {
                    optName = modelOperate.code();
                }
                if(ObjectUtils.isEmpty(optName)) {
                    optName = method.getName();
                }

//                String optName = StringUtils.isEmpty(modelOperate.name()) ? method.getName() : modelOperate.name();
                String moduleCode = StringUtils.isEmpty(modelResource.code()) ? typeClass.getName() : modelResource.code();
                String optCode = StringUtils.isEmpty(modelOperate.code()) ? method.getName(): modelOperate.code();
                String group = StringUtils.isEmpty(modelOperate.group()) ? null : modelOperate.group();
                String version = StringUtils.isEmpty(modelOperate.version()) ? null : modelOperate.version();
                OperateInfo operateInfo = new OperateInfo(moduleCode + OperateInfo.CODE_SEPARATOR + optCode,request.getRequestURI(),request.getMethod(),request.getRemoteHost(),moduleName, optName,group,version);
                auditInfoThreadLocal.set(operateInfo);
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        if(operateInfoHandler != null){
            int status = response.getStatus();

            OperateInfo operateInfo = auditInfoThreadLocal.get();
            OperateIdentification oid = OidHolder.getOperateIdentification();
            if(operateInfo != null){
                try{
                    operateInfoHandler.doHandle((ex == null && status < 400), operateInfo,oid.getOperateObjectList());
                }finally {
                    auditInfoThreadLocal.remove();
                }
            }
        }
    }

}
