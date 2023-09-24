package cn.kduck.core.web.interceptor;

import cn.kduck.core.web.resolver.ValidErrorArgumentResolver;
import cn.kduck.core.web.validation.ConstraintValidator;
import cn.kduck.core.web.validation.ValidError;
import cn.kduck.core.web.validation.ValidError.ValidErrorField;
import cn.kduck.core.web.validation.ValidatorFactory;
import cn.kduck.core.service.ValueMap;
import cn.kduck.core.utils.RequestUtils;
import cn.kduck.core.web.validation.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * LiuHG
 */
public class ValidationInterceptor implements HandlerInterceptor {

    private PropertyPlaceholderHelper placeholderHelper = new PropertyPlaceholderHelper("{","}");
//    private ApplicationContext applicationContext;

//    private LicenseService licenseService;

    public ValidationInterceptor(ApplicationContext applicationContext){
//        this.applicationContext = applicationContext;
//        this.licenseService = applicationContext.getBean(LicenseService.class);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

//        checkLicense(request,response);

        if(handler instanceof HandlerMethod){
            HandlerMethod handlerMethod = ((HandlerMethod)handler);
            Annotation[] declaredAnnotations = AnnotationUtils.getAnnotations(handlerMethod.getMethod());
            ValueMap parameterMap = RequestUtils.getParameterMap(request);
            List<ValidErrorField> errorList = new ArrayList<>();
            for (Annotation anno : declaredAnnotations) {
                ConstraintValidator validator = ValidatorFactory.getValidator(anno.annotationType());
                if(validator != null){
                    String[] names = (String[]) AnnotationUtils.getValue(anno, "value");
                    for (String attrName : names) {
                        String v = parameterMap.getValueAsString(attrName);
                        if(!validator.isValid(attrName,v,anno)){
                            String message = (String) AnnotationUtils.getValue(anno, "message");
                            Map<String, Object> annoAttrs = AnnotationUtils.getAnnotationAttributes(anno);

                            Properties properties = map2Properties(annoAttrs);
                            properties.setProperty("name",attrName);
                            properties.remove("value");
                            errorList.add(new ValidErrorField(attrName, placeholderHelper.replacePlaceholders(message,properties)));
                        }
                    }
                }
            }

            if(!errorList.isEmpty()){
                boolean processed = false;
                Class<?>[] parameterTypes = handlerMethod.getMethod().getParameterTypes();
                for (Class<?> parameterType : parameterTypes) {
                    if(parameterType == ValidError.class){
                        processed = true;
                        break;
                    }
                }
                if(!processed){
                    String errorMessage = errorList.stream().map(ValidErrorField::getMessage).collect(Collectors.joining(","));
                    throw new ValidationException("字段校验失败："+errorMessage,errorList);
                }
            }

            if(!errorList.isEmpty()){
                request.setAttribute(ValidErrorArgumentResolver.VALID_ERROR,errorList);
            }
        }
        return true;
    }

//    private void checkLicense(HttpServletRequest request, HttpServletResponse response) {
//        String requestURI = request.getRequestURI();
//        int extIndex = requestURI.lastIndexOf(".");
//        String[] staticExtName = {".js",".png",".css",".jpg",".gif"};
//        if(extIndex >= 0){
//            String extName = requestURI.substring(extIndex);
//            if(StringUtils.contain(staticExtName,extName)){
//                return;
//            }
//        }
//
//        try {
//            licenseService.verify();
//        } catch (Exception e) {
//            applicationContext.publishEvent(new VerifyEvent(VerifyEvent.NO_LICENSE,e,request,response));
//        }
//    }

    private Properties map2Properties(Map<String, Object> map){
        Properties properties = new Properties();
        Iterator<String> iterator = map.keySet().iterator();
        while(iterator.hasNext()){
            String key = iterator.next();
            properties.put(key,String.valueOf(map.get(key)));
        }
        return properties;
    }

}
