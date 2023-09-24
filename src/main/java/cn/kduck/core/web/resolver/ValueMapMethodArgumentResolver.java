package cn.kduck.core.web.resolver;

import cn.kduck.core.service.ValueMap;
import cn.kduck.core.utils.RequestUtils;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.Assert;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.ModelAttributeMethodProcessor;

import java.util.List;
import java.util.Map;

/**
 * LiuHG
 */
public class ValueMapMethodArgumentResolver extends ModelAttributeMethodProcessor {

    protected static final String FORM_CONTENT_TYPE = "application/x-www-form-urlencoded";

    private final List<HttpMessageConverter<?>> messageConverters;

//    private final AutofillValue autofillValue;

    public ValueMapMethodArgumentResolver(List<HttpMessageConverter<?>> messageConverters){
        super(true);
        this.messageConverters = messageConverters;
//        this.autofillValue = autofillValue;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return ValueMap.class.isAssignableFrom(parameter.getParameterType());
    }

//    @Override
//    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
//        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
//
//        Class<?> parameterType = parameter.getParameterType();
//        Object arg = BeanUtils.instantiateClass(parameterType);
//
//        ((Map)arg).putAll(RequestUtils.getParameterMap(request));
//
//        return arg;
//    }


    @Override
    protected Object createAttribute(String attributeName, MethodParameter parameter, WebDataBinderFactory binderFactory, NativeWebRequest webRequest) throws Exception {

        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);

        String contentType = request.getContentType();

        Class<?> parameterType = parameter.getParameterType();
        Object arg = BeanUtils.instantiateClass(parameterType);

        Map parameterMap = null;
        if(contentType == null || contentType.contains(FORM_CONTENT_TYPE)){
            parameterMap = RequestUtils.getParameterMap(request);
//            if(parameterType != ValueMap.class){
//                Iterator nameIterator = parameterMap.keySet().iterator();
//                while(nameIterator.hasNext()){
//                    Object name = nameIterator.next();
//                    Class<?> propertyType = BeanUtils.findPropertyType(name.toString(), parameterType);
//                    if(propertyType != null){
//                        Object value = parameterMap.get(name);
//                        parameterMap.put(name,ConversionUtils.convert(value,propertyType));
//                    }
//                }
//
////                PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(parameterType);
////                for (PropertyDescriptor p : propertyDescriptors) {
////                    if("class".equals(p.getName())){
////                        continue;
////                    }
////                    Object value = parameterMap.get(p.getName());
////                    Method readMethod = p.getReadMethod();
////                    if(readMethod != null){
////                        Class<?> type = readMethod.getReturnType();
////                        parameterMap.put(p.getName(),ConversionUtils.convert(value,type));
////                    }
////                }
//            }
        }else{
            for (HttpMessageConverter messageConverter: messageConverters) {
                if(messageConverter.canRead(Map.class, MediaType.APPLICATION_JSON)){
                    parameterMap = new ValueMap();
                    parameterMap.putAll((Map)messageConverter.read(Map.class, new ServletServerHttpRequest(request)));
                }
            }
        }

        ValueMap valueMap = (ValueMap)arg;
        if(parameterMap != null){
            valueMap.putAll(parameterMap);
        }

//        if(autofillValue != null){
//            autofillValue.autofill(valueMap);
//        }

        return arg;
    }

    @Override
    protected void bindRequestParameters(WebDataBinder binder, NativeWebRequest request) {
        ServletRequest servletRequest = request.getNativeRequest(ServletRequest.class);
        Assert.state(servletRequest != null, "No ServletRequest");
        ServletRequestDataBinder servletBinder = (ServletRequestDataBinder) binder;
        servletBinder.bind(servletRequest);
    }

}
