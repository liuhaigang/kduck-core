package cn.kduck.core.web.swagger;

import com.fasterxml.classmate.ResolvedType;
import org.springframework.util.StringUtils;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ResolvedMethodParameter;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.singleton;

/**
 * DocumentationPluginsManager
 * SwaggerResponseMessageReader和ResponseMessagesReader会重置responseMessages
 * @author LiuHG
 */
//@Component
//@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER + 5)
public class OperationValueMapReader implements OperationBuilderPlugin {

    private Set<ResponseMessage> responseMessages(String respones) {
        return singleton(new ResponseMessageBuilder()
                .code(200)
                .message("成功")
                .responseModel(new ModelRef(respones + "ResJsonObject"))
                .build());
    }

    @Override
    public void apply(OperationContext context) {
        Optional<ApiJsonResponse> apiValueMap = context.findAnnotation(ApiJsonResponse.class);
        if (apiValueMap.isPresent()){
            ApiJsonResponse apiEntityAnno = apiValueMap.get();
            String name = apiEntityAnno.name();
            Class type = apiEntityAnno.type();
            name = StringUtils.isEmpty(name) ? context.getName() + getClassParamName(context) : name;
            name = type != Class.class ? type.getSimpleName() : name;//如果配置了Class则优先级是用Class
            context.operationBuilder().responseMessages(responseMessages(name));

        }
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
