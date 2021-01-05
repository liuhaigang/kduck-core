package cn.kduck.core.web.swagger;

import com.fasterxml.classmate.ResolvedType;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ResolvedMethodParameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.ParameterBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.spi.service.contexts.ParameterContext;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

/**
 * 处理JSON对象形式提交的参数
 * @author LiuHG
 * @see ParameterValueMapReader
 */
@Component
@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER)
public class JsonObjectValueMapReader implements ParameterBuilderPlugin {

    @Override
    public void apply(ParameterContext parameterContext) {
        ResolvedMethodParameter parameter = findParameter(parameterContext, RequestBody.class);
        if(parameter == null){
            return;
        }

        /**
         * 如果被注解@RequestBody注解的属性名与当前处理的属性名不相同，则忽略，不执行。
         */
        if(!parameter.defaultName().equals(parameterContext.resolvedMethodParameter().defaultName())){
            return;
        }

        Optional<ApiJsonRequest> apiValueMap = parameterContext.getOperationContext().findAnnotation(ApiJsonRequest.class);
        if (apiValueMap.isPresent()){
            ApiJsonRequest apiEntityAnno = apiValueMap.get();
            String name = parameterContext.getOperationContext().getName() + getClassParamName(parameterContext.getOperationContext());
            if(StringUtils.hasText(apiEntityAnno.name())){
                name = apiEntityAnno.name();
            }
            name = name + "Req";
            //如果配置了Class则优先级是用Class,并且由于是直接是用的Class类原始类，因此不能增加"Req"后缀
            name = apiEntityAnno.type() != Class.class ? apiEntityAnno.type().getSimpleName() : name;
//            String paramName = getParamName(parameterContext);
            parameterContext.parameterBuilder()
                    .parameterType("body").modelRef(new ModelRef(name));
        }
    }

//    private String getParamName(ParameterContext parameterContext){
//        ResolvedMethodParameter parameter = findParameter(parameterContext, RequestBody.class);
//        Optional<RequestParam> requestParam = parameter.findAnnotation(RequestParam.class);
//        if(requestParam.isPresent()){
//            return (String)AnnotationUtils.getValue(requestParam.get(),"value");
//        }else{
//            return parameter.defaultName().get();
//        }
//    }

    /**
     * 查找第一个匹配注解的参数
     * @param parameterContext
     * @param annoClasses
     * @return
     */
    private ResolvedMethodParameter findParameter(ParameterContext parameterContext,Class<? extends Annotation>... annoClasses){
        List<ResolvedMethodParameter> parameters = parameterContext.getOperationContext().getParameters();
        if(!CollectionUtils.isEmpty(parameters)){
            for (ResolvedMethodParameter p : parameters) {
                boolean hasAnno = true;
                for (Class<? extends Annotation> annoClass : annoClasses) {
                    Optional<? extends Annotation> annotation = p.findAnnotation(annoClass);
                    if(!annotation.isPresent()){
                        hasAnno = false;
                        break;
                    }
                }
                if(hasAnno){
                    return p;
                }
            }
        }
        return null;
    }

    private <T extends Annotation> T findAnnotation(ParameterContext parameterContext,Class<T> annoClass){
        List<ResolvedMethodParameter> parameters = parameterContext.getOperationContext().getParameters();
        if(!CollectionUtils.isEmpty(parameters)){
            for (ResolvedMethodParameter p : parameters) {
                Optional<T> annotation = p.findAnnotation(annoClass);
                if(annotation.isPresent()){
                    return annotation.get();
                }
            }
        }
        return null;
    }

    private String getClassParamName(OperationContext context) {
        List<ResolvedMethodParameter> parameters = context.getParameters();
        StringBuilder paramNames = new StringBuilder();
        for (ResolvedMethodParameter parameter : parameters) {
            ResolvedType parameterType = parameter.getParameterType();
            String simpleName = parameterType.getErasedType().getSimpleName();
            if(parameterType.isArray()){
                if(simpleName.endsWith("[]")) {
                    simpleName = simpleName.substring(0,simpleName.length()-2);
                }
                paramNames.append(simpleName+"s");
            }else{
                paramNames.append(simpleName);
            }
        }
        return paramNames.toString();
    }

    @Override
    public boolean supports(DocumentationType delimiter) {
        return true;
    }
}
