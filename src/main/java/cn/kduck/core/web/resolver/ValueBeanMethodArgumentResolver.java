package cn.kduck.core.web.resolver;

import cn.kduck.core.dao.definition.BeanDefDepository;
import cn.kduck.core.dao.definition.BeanEntityDef;
import cn.kduck.core.web.annotation.RequestValueBean;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import cn.kduck.core.service.ValueBean;
import cn.kduck.core.utils.RequestUtils;

import java.util.List;
import java.util.Map;

/**
 * LiuHG
 */
public class ValueBeanMethodArgumentResolver implements HandlerMethodArgumentResolver {

    protected static final String FORM_CONTENT_TYPE = "application/x-www-form-urlencoded";

    private final BeanDefDepository beanDefDepository;
    private final List<HttpMessageConverter<?>> messageConverters;

    public ValueBeanMethodArgumentResolver(BeanDefDepository beanDefDepository, List<HttpMessageConverter<?>> messageConverters){
        this.beanDefDepository = beanDefDepository;
        this.messageConverters = messageConverters;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(RequestValueBean.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);

//        RequestValueBean valueBeanAnno = parameter.getParameterAnnotation(RequestValueBean.class);
//        BeanEntityDef entityDef = beanDefDepository.getEntityDef(valueBeanAnno.value());
//        if(entityDef == null){
//            //FIXME 具体异常
//            throw new RuntimeException("Bean未定义："+valueBeanAnno.value());
//        }
//        String contentType = request.getContentType();
//        MediaType mediaType = contentType == null ? null : MediaType.parseMediaType(contentType);
//        for (HttpMessageConverter messageConverter: messageConverters) {
//            if(messageConverter.canRead(Map.class, mediaType)){
//                Map parameterMap = (Map)messageConverter.read(Map.class, new ServletServerHttpRequest(request));
//                if(parameterMap == null){
//
//                }
//                return new ValueBean(entityDef,parameterMap,false);
//            }
//        }

        if(HttpMethod.POST.matches(request.getMethod()) || HttpMethod.PUT.matches(request.getMethod())){
            String contentType = request.getContentType();
            RequestValueBean valueBeanAnno = parameter.getParameterAnnotation(RequestValueBean.class);
            BeanEntityDef entityDef = beanDefDepository.getEntityDef(valueBeanAnno.value());
            if(entityDef == null){
                //FIXME 具体异常
                throw new RuntimeException("Bean未定义："+valueBeanAnno.value());
            }

            if(contentType == null || contentType.contains(FORM_CONTENT_TYPE)){
                Map<String, Object> parameterMap = RequestUtils.getParameterMap(request);
                return new ValueBean(entityDef,parameterMap,false);
            }else{
                for (HttpMessageConverter messageConverter: messageConverters) {
                    if(messageConverter.canRead(Map.class, MediaType.APPLICATION_JSON)){
                        Map parameterMap = (Map)messageConverter.read(Map.class, new ServletServerHttpRequest(request));
                        return new ValueBean(entityDef,parameterMap,false);
                    }
                }
            }
        }
        throw new IllegalArgumentException("无法构造ValueBean方法参数对象");
    }
}
