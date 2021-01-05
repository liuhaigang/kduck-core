package cn.kduck.core.web.swagger;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.AllowableValues;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.spring.web.DescriptionResolver;
import springfox.documentation.swagger.readers.operation.OperationImplicitParameterReader;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Strings.emptyToNull;
import static org.slf4j.LoggerFactory.getLogger;
import static springfox.documentation.schema.Types.isBaseType;
import static springfox.documentation.swagger.common.SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER;
import static springfox.documentation.swagger.common.SwaggerPluginSupport.pluginDoesApply;
import static springfox.documentation.swagger.schema.ApiModelProperties.allowableValueFromString;

/**
 * 处理由@ApiParamRequest注解标注的k-v参数形式的注解文档，该注解与@ApiImplicitParams注解功能类似，用法也类似，
 * 只是为了开发者使用时能够统一代码风格而存在。
 * @author LiuHG
 * @see JsonObjectValueMapReader
 */
//@Component
//@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER)
public class ParameterValueMapReader implements OperationBuilderPlugin {

    private static final Logger LOGGER = getLogger(OperationImplicitParameterReader.class);

    private final DescriptionResolver descriptions;

    @Autowired
    public ParameterValueMapReader(DescriptionResolver descriptions) {
        this.descriptions = descriptions;
    }

//    @Override
//    public void apply(OperationContext context) {
////        context.operationBuilder().parameters(readParameters(context));
//        List<Compatibility<springfox.documentation.service.Parameter, RequestParameter>> parameters
//                = readParameters(context);
//        context.operationBuilder().parameters(parameters.stream()
//                .map(Compatibility::getLegacy)
//                .filter(Optional::isPresent)
//                .map(Optional::get)
//                .collect(Collectors.toList()));
//        context.operationBuilder().requestParameters(parameters.stream()
//                .map(Compatibility::getModern)
//                .filter(Optional::isPresent)
//                .map(Optional::get)
//                .collect(Collectors.toList()));
//    }

    @Override
    public boolean supports(DocumentationType delimiter) {
        return pluginDoesApply(delimiter);
    }

    @Override
    public void apply(OperationContext context) {
        context.operationBuilder().parameters(readParameters(context));
    }

//    private List<Compatibility<springfox.documentation.service.Parameter, RequestParameter>>
//    readParameters(OperationContext context) {
//        Optional<ApiField> annotation = context.findAnnotation(ApiField.class);
//        List<Compatibility<springfox.documentation.service.Parameter, RequestParameter>> parameters = new ArrayList<>();
//        annotation.ifPresent(
//                apiField ->
//                        parameters.add(
//                                ParameterValueMapReader.implicitParameter(descriptions, apiField)));
//        return parameters;
//    }
////ApiField
//    static Compatibility<Parameter, RequestParameter> implicitParameter(
//            DescriptionResolver descriptions,
//            ApiField param) {
//        Compatibility<springfox.documentation.schema.ModelRef, ModelSpecification> modelRef = maybeGetModelRef(param);
//        ParameterType in = ParameterType.from(param.paramType());
//        return new Compatibility<>(
//                new springfox.documentation.builders.ParameterBuilder()
//                        .name(param.name())
//                        .description(descriptions.resolve(param.value()))
//                        .defaultValue(param.defaultValue())
//                        .required(param.required())
//                        .allowMultiple(param.allowMultiple())
//                        .modelRef(modelRef.getLegacy().orElse(null))
//                        .allowableValues(allowableValueFromString(param.allowableValues()))
//                        .parameterType(ofNullable(param.paramType())
//                                .filter(((Predicate<String>) String::isEmpty).negate())
//                                .orElse(null))
//                        .parameterAccess(param.access())
//                        .order(SWAGGER_PLUGIN_ORDER)
//                        .scalarExample(param.example())
////                        .complexExamples(examples(param.examples()))
//                        .collectionFormat(param.collectionFormat())
//                        .build(),
//                new RequestParameterBuilder()
//                        .name(param.name())
//                        .description(descriptions.resolve(param.value()))
//                        .required(param.required())
//                        .in(in)
////            .allowMultiple(param.allowMultiple())
//                        .query(q -> q.model(m -> modelRef.getModern().ifPresent(m::copyOf))
//                                .defaultValue(param.defaultValue())
//                                .enumerationFacet(e -> e.allowedValues(allowableValueFromString(param.allowableValues())))
//                                .collectionFacet(c -> c.collectionFormat(
//                                        CollectionFormat.convert(param.collectionFormat())
//                                                .orElse(null))))
//                        .precedence(SWAGGER_PLUGIN_ORDER)
//                        .example(new ExampleBuilder().value(param.example()).build())
////                        .examples(examples(param.examples()).entrySet().stream()
////                                .flatMap(e -> e.getValue().stream())
////                                .collect(Collectors.toList()))
//                        .build()
//        );
//    }
//
//    private static Compatibility<springfox.documentation.schema.ModelRef, ModelSpecification>
//    maybeGetModelRef(ApiField param) {
//        String dataType = ofNullable(param.dataType())
//                .filter(((Predicate<String>) String::isEmpty).negate())
//                .orElse("string");
//        ModelSpecification modelSpecification = modelSpecification(param);
//
//        AllowableValues allowableValues = null;
//        if (springfox.documentation.schema.Types.isBaseType(dataType)) {
//            allowableValues = allowableValueFromString(param.allowableValues());
//        }
//        if (param.allowMultiple()) {
//            return new Compatibility<>(
//                    new springfox.documentation.schema.ModelRef("",
//                            new springfox.documentation.schema.ModelRef(dataType, allowableValues)),
//                    modelSpecification);
//        }
//        return new Compatibility<>(
//                new springfox.documentation.schema.ModelRef(dataType, allowableValues), modelSpecification);
//    }
//
//    private static ModelSpecification modelSpecification(
//            ApiField param) {
//        Class<?> clazz;
//        try {
//            param.dataTypeClass();
//            if (param.dataTypeClass() != Void.class) {
//                clazz = param.dataTypeClass();
//            } else {
//                clazz = Class.forName(param.dataType());
//            }
//        } catch (ClassNotFoundException e) {
//            LOGGER.warn(
//                    "Unable to interpret the implicit parameter configuration with dataType: {}, dataTypeClass: {}",
//                    param.dataType(),
//                    param.dataTypeClass());
//            return null;
//        }
//        ModelSpecification modelSpecification = ScalarTypes.builtInScalarType(clazz)
//                .map(scalar -> {
//                    if (param.allowMultiple()) {
//                        return new ModelSpecificationBuilder()
//                                .collectionModel(c ->
//                                        c.model(m ->
//                                                m.scalarModel(scalar))
//                                                .collectionType(CollectionType.LIST))
//                                .build();
//                    }
//                    return new ModelSpecificationBuilder()
//                            .scalarModel(scalar)
//                            .build();
//                })
//                .orElse(null);
//        if (modelSpecification == null) {
//            ModelKey dataTypeKey = new ModelKeyBuilder()
//                    .qualifiedModelName(q -> q.namespace(safeGetPackageName(clazz)).name(clazz.getSimpleName()))
//                    .build();
//            modelSpecification = referenceModelSpecification(dataTypeKey, param.allowMultiple());
//        }
//        return modelSpecification;
//    }
//
//    private static ModelSpecification referenceModelSpecification(
//            ModelKey dataTypeKey,
//            boolean allowMultiple) {
//        if (allowMultiple) {
//            return new ModelSpecificationBuilder()
//                    .collectionModel(c ->
//                            c.model(m ->
//                                    m.referenceModel(r ->
//                                            r.key(k ->
//                                                    k.copyOf(dataTypeKey))))
//                                    .collectionType(CollectionType.LIST))
//                    .build();
//        }
//        return new ModelSpecificationBuilder()
//                .referenceModel(r -> r.key(k -> k.copyOf(dataTypeKey)))
//                .build();
//    }

    private List<Parameter> readParameters(OperationContext context) {
        Optional<ApiParamRequest> annotation = context.findAnnotation(ApiParamRequest.class);

        List<Parameter> parameters = Lists.newArrayList();
        if (annotation.isPresent()) {
            for (ApiField param : annotation.get().value()) {
                parameters.add(implicitParameter(descriptions, param));
            }
        }

        return parameters;
    }

    private Parameter implicitParameter(DescriptionResolver descriptions, ApiField param) {
        ModelRef modelRef = maybeGetModelRef(param);
        return new ParameterBuilder()
                .name(param.name())
                .description(descriptions.resolve(param.value()))
                .defaultValue(param.defaultValue())
                .required(param.required())
                .allowMultiple(param.allowMultiple())
                .modelRef(modelRef)
                .allowableValues(allowableValueFromString(param.allowableValues()))
                .parameterType(emptyToNull(param.paramType()))
//                .parameterAccess(param.access())
                .order(SWAGGER_PLUGIN_ORDER)
                .scalarExample(param.example())
//                .complexExamples(examples(param.examples()))
                .build();
    }

    private ModelRef maybeGetModelRef(ApiField param) {
        String dataType = MoreObjects.firstNonNull(emptyToNull(param.dataType()), "string");
        AllowableValues allowableValues = null;
        if (isBaseType(dataType)) {
            allowableValues = allowableValueFromString(param.allowableValues());
        }
        if (param.allowMultiple()) {
            return new ModelRef("", new ModelRef(dataType, allowableValues));
        }
        return new ModelRef(dataType, allowableValues);
    }

}
