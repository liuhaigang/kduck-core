package cn.kduck.core.web.swagger;

import cn.kduck.core.service.ParamMap;
import cn.kduck.core.dao.definition.BeanDefDepository;
import cn.kduck.core.dao.definition.BeanEntityDef;
import cn.kduck.core.dao.definition.BeanFieldDef;
import cn.kduck.core.web.json.JsonObject;
import cn.kduck.core.web.json.JsonPageObject;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import cn.kduck.core.utils.StringUtils;
import cn.kduck.core.utils.ValueMapUtils;
import io.swagger.annotations.ApiModelProperty;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtPrimitiveType;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.BooleanMemberValue;
import javassist.bytecode.annotation.IntegerMemberValue;
import javassist.bytecode.annotation.StringMemberValue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import springfox.documentation.service.ResolvedMethodParameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationModelsProviderPlugin;
import springfox.documentation.spi.service.contexts.RequestMappingContext;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author LiuHG
 */
//@Component
//@Order(Ordered.HIGHEST_PRECEDENCE)
public class OperationModelValueMapReader implements OperationModelsProviderPlugin {

    private final Log logger = LogFactory.getLog(getClass());

    private final static String BASE_PACKAGE = "com.goldgov.swagger.";

    /**
     * 是否允许根级中介类同名
     * 如果允许重复，会导致方法名相同，但属性定义不同时，会出现属性混在一起的情况。
     * 但有些Controller相互继承的场景，同名的方法属性定义也相同时，可以开启此开关。
     */
    @Value("${kduck.swagger.allow-name-repeat:false}")
    private boolean allowNameRepeat;

    private int serial = 1;//为类包新增一段序列报名，避免类重复

    private final static String CLASS_SUFFIX = "$Class";
    private final static String ARRAY_CLASS_SUFFIX = "$ArrayClass";

    /*
     * 将因Swagger注解形成的中介类，以"类名->Class"的关系缓存起来，便于使用。
     */
    private Map<String,Class> classMap = new HashMap<>();

    private Map<CtClass,ClassFile> classFileMap = new HashMap<>();

    private final TypeResolver typeResolver;

    private BeanDefDepository beanDefDepository;

    public OperationModelValueMapReader(TypeResolver typeResolver, BeanDefDepository beanDefDepository){
        this.typeResolver = typeResolver;
        this.beanDefDepository = beanDefDepository;
    }

    @Override
    public void apply(RequestMappingContext context) {

//        由于注解都是打在方法上，默认是以方法名为类名进行创建，因此为了区分，在生成请求的类名后加上Req后缀，
//        生成响应的类名后加上Res后缀给予区分。如果类名发生冲突，则考虑用注解的name属性来定义类名（model名）

        //处理方法参数的Json对象
        Optional<ApiJsonRequest> requestBody = context.findAnnotation(ApiJsonRequest.class);
        if(requestBody.isPresent()){
            ApiJsonRequest apiJsonRequest = requestBody.get();
            String className = getClassName(context, apiJsonRequest.name())+"Req";

            //如果配置了Class，则直接使用
            if(apiJsonRequest.type() != Class.class){
                context.getDocumentationContext().getAdditionalModels()
                        .add(typeResolver.resolve(apiJsonRequest.type()));
//                context.operationModelsBuilder().addInputParam(typeResolver.resolve(apiJsonRequest.type()));
            }else{
                Class<?> clazz;
                try {
                    clazz = Class.forName(getSerialPackageFullName() + className);
                } catch (Exception e) {
                    clazz = processJsonRequest(className, apiJsonRequest);
                }
                if(clazz != null){
                    context.getDocumentationContext().getAdditionalModels()
                            .add(typeResolver.resolve(clazz));
//                    context.operationModelsBuilder().addInputParam(typeResolver.resolve(clazz));
                }
            }

        }

        //处理返回值的Json对象
        Optional<ApiJsonResponse> optional = context.findAnnotation(ApiJsonResponse.class);
        if(optional.isPresent()){
            ApiJsonResponse apiJsonBody = optional.get();

            if(classMap.containsKey(apiJsonBody.name())) return;

            Class<?> returnType = context.getReturnType().getErasedType();

            String className = getClassName(context, apiJsonBody.name()) + "Res";
            if(apiJsonBody.type() != Class.class){
                className = apiJsonBody.type().getSimpleName() + "Res";
            }

            //处理JsonObject或JsonPageObject，因为返回格式不同，需要分别处理
            Class<?> clazz;
            if(returnType == JsonPageObject.class){
                clazz = processJsonPageObject(className, apiJsonBody);
            }else if(returnType == JsonObject.class){
                boolean isArray = apiJsonBody.isArray();
                clazz = processJsonObject(className, apiJsonBody,isArray);
            }else{
                clazz = returnType;
            }

            if(clazz != null){
//                context.getDocumentationContext().getAdditionalModels()
//                        .add(typeResolver.resolve(clazz));
                context.operationModelsBuilder().addReturn(typeResolver.resolve(clazz));
            }

        }

        classMap.clear();//清除缓存的中介类
    }

    /**
     * 得到用于类名的属性，如果注解中未指定name，则是用方法名
     * @param context
     * @param filedName 注解中定义的属性名
     * @return
     */
    private String getClassName(RequestMappingContext context, String filedName) {
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
        String className = context.getName() + paramNames;
        if(org.springframework.util.StringUtils.hasText(filedName)){
            className = filedName;
        }
        return className;
    }

    /**
     * 处理JsonPageObject对象，返回的JsonPageObject的中介类，以"JsonObject"结尾
     * @param name 不含路径的中介类名
     * @param apiJsonBody
     * @return
     */
    private Class<?> processJsonPageObject(String name, ApiJsonResponse apiJsonBody){
        processJsonRespones(name, apiJsonBody);
//        String className = getSerialPackageFullName() + name + "JsonObject";
        String className =  name + "JsonObject";

        try{
            return Class.forName(BASE_PACKAGE + className);
        }catch (Exception e){}


//        ClassPool pool = ClassPool.getDefault();
//        CtClass ctClass = pool.makeClass(className);
        CtClass ctClass = getCtClass(className);

        processStdField(name, ctClass,true);
        try {
            ctClass.addMethod(createReadMethod(Integer.class,"pageSize","每页显示数量",ctClass));
            ctClass.addMethod(createReadMethod(Integer.class,"count","总条数",ctClass));
            ctClass.addMethod(createReadMethod(Integer.class,"maxPage","总页数",ctClass));
        } catch (CannotCompileException e) {
            throw new RuntimeException("创建标准JsonPageObject属性方法时出错",e);
        }

        try {
            Class<?> clazz = ctClass.toClass();
            classMap.put(name,clazz);
            return clazz;
        } catch (CannotCompileException e) {
            throw new RuntimeException("编译Class时发生异常",e);
        }
    }

    /**
     * 处理JsonObject对象，返回的JsonObject的中介类，以"JsonObject"结尾
     * @param name
     * @param apiJsonBody
     * @param isArray
     * @return
     */
    private Class<?> processJsonObject(String name, ApiJsonResponse apiJsonBody,boolean isArray){

        processJsonRespones(name, apiJsonBody);
//        String className = getSerialPackageFullName() + name + "JsonObject";
        String className = name + "JsonObject";

        try{
            return Class.forName(BASE_PACKAGE + className);
        }catch (Exception e){}

//        ClassPool pool = ClassPool.getDefault();
//        CtClass ctClass = pool.makeClass(className);
        CtClass ctClass = getCtClass(className);

        processStdField(name, ctClass,isArray);
        try {
            Class<?> clazz = ctClass.toClass();
            classMap.put(name,clazz);
            return clazz;
        } catch (CannotCompileException e) {
            throw new RuntimeException("编译Class时发生异常",e);
        }
    }

    /**
     * 处理标准的JsonObject字段：code、message、data
     * @param name
     * @param ctClass
     * @param dataArray
     */
    private void processStdField(String name, CtClass ctClass,boolean dataArray) {
        try {
            ctClass.addMethod(createReadMethod(Integer.class,"code","状态码",ctClass));
//            ctClass.addMethod(createWriteMethod(Integer.class,"code", ctClass));

            ctClass.addMethod(createReadMethod(String.class,"message","消息内容",ctClass));
//            ctClass.addMethod(createWriteMethod(String.class,"message", ctClass));

            Class aClass = classMap.get(name);
            if(dataArray){
                aClass = Array.newInstance(aClass, 0).getClass();
            }

            if(aClass == null){
                aClass = Class.class;
            }
            ctClass.addMethod(createReadMethod(aClass,"data","数据对象", ctClass));
//            ctClass.addMethod(createWriteMethod(aClass1,"data", ctClass));
        } catch (CannotCompileException e) {
            throw new RuntimeException("创建标准JsonObject属性方法时出错",e);
        }
    }

    private Class<?> processJsonRequest(String name, ApiJsonRequest apiJsonBody){
        Map<String, Object> annotationAttributes = AnnotationUtils.getAnnotationAttributes(apiJsonBody);
        return processEntity(name,annotationAttributes);
    }

    private Class<?> processJsonRespones(String name, ApiJsonResponse apiJsonBody){
        Map<String, Object> annotationAttributes = AnnotationUtils.getAnnotationAttributes(apiJsonBody);

        try{
            return Class.forName(BASE_PACKAGE + name);
        }catch (Exception e){}

        return processEntity(name,annotationAttributes);
    }

    /**
     * 处理每个@ApiJsonRequest或@ApiJsonResponse的@ApiField注解
     * @param name 不含路径的中介类名
     * @param annotationAttributes @ApiJsonRequest或@ApiJsonResponse对象中所有注解属性
     * @return
     */
    private Class<?> processEntity(String name, Map<String, Object> annotationAttributes){

        String code = ValueMapUtils.getValueAsString(annotationAttributes, "code");
        Class type = (Class) annotationAttributes.get("type");

        if(classMap.containsKey(name)) return null;

        //处理情况一：处理指定了class的情况
        if(Class.class != type){
            classMap.put(name,type);
            return type;
        }

//        String className = getSerialPackageFullName() + name;
//        String className = BASE_PACKAGE + name;


        ApiField[] fields = (ApiField[]) annotationAttributes.get("value");

        CtClass ctClass = getCtClass(name);

        logger.debug("创建根中介类：" + ctClass.getName());

        //处理情况二：处理指定了实体编码的情况
        if(org.springframework.util.StringUtils.hasText(code)){
            BeanEntityDef entityDef = beanDefDepository.getEntityDef(code);
            List<BeanFieldDef> fieldList = entityDef.getFieldList();

            String[] include = (String[]) annotationAttributes.get("include");
            for (BeanFieldDef beanFieldDef : fieldList) {
                if(include.length > 0){
                    if(!StringUtils.contain(include,beanFieldDef.getAttrName())){
                        continue;
                    }
                }
                Map<String, Object> attributes = new HashMap<>();
                attributes.put("name",beanFieldDef.getAttrName());
                attributes.put("value",beanFieldDef.getRemarks());
                attributes.put("dataType",beanFieldDef.getJavaType().getSimpleName());

                CtMethod readMethod = createReadMethod(attributes, ctClass);
                CtMethod writeMethod = createWriteMethod(attributes, ctClass);
                try {
                    ctClass.addMethod(readMethod);
                    ctClass.addMethod(writeMethod);
                } catch (CannotCompileException e) {
                    throw new RuntimeException("为class添加方法时发生错误",e);
                }
            }
            try {
                ClassFile classFile = ctClass.getClassFile();
                Class<?> clazz = ctClass.toClass();
                classMap.put(name,clazz);
                classFileMap.put(ctClass,classFile);
                return clazz;
            } catch (CannotCompileException e) {
                throw new RuntimeException("根据code方式生成Swagger文档，编译Class时发生异常",e);
            }
        }else

        //处理情况三：处理指定了具体字段的情况
        if(fields.length > 0){
            Map<String, Object> fieldMap = formatField(fields);
            Class clazz = createClass("innerclass", ctClass, fieldMap,false);
            classMap.put(name,clazz);
            return clazz;

        }

        throw new RuntimeException("无法通过生成ApiJsonBody注解生成swagger文档，至少提供code属性或fields属性中的一个");
    }

    private Class createClass(String packageName,CtClass parent,Map<String, Object> fieldMap,boolean isArray){
        Iterator<String> keys = fieldMap.keySet().iterator();

        //提前将类名装载到一个List中，便于循环时可能会修改Map对象而导致并发修改异常
        List<String> classNameList = new ArrayList<>(fieldMap.size());
        while(keys.hasNext()){
            classNameList.add(keys.next());
        }

        for (String name : classNameList) {
            Map<String, Object> attributes = null;
            if(name.endsWith(CLASS_SUFFIX) || name.endsWith(ARRAY_CLASS_SUFFIX)){
                ClassPool pool = ClassPool.getDefault();
                String actualName = name.endsWith("[]") ? name.substring(0, name.length() -2):name;

                //****************************
                /* 在特殊场景，例如：
                 * {
                 *   @ApiField(name="a.b.c.attrName1",value="属性1"),
                 *   @ApiField(name="a.b.attrName2",value="属性2"),
                 *  }
                 * 由于在处理第一个@ApiField注解时，会生成a、b、c的中介类，当处理第二个@ApiField时，a存在，直接复用，但到了b，
                 * 需要为这个对象新增属性attrName2的getter方法，但这个b类在之前已经创建，并冻结结构不允许修改，会抛异常。
                 *
                 * 下面的处理逻辑是：
                 *  如果遇到中介类已经在之前有创建，则插入一个"$+seqNum"的后缀，然后重新生成一个中介类，不与之前的产生冲突。
                 *  这里说插入后缀并不是放在类名最后，例如原类名是：User$Class，插入后缀后是：User$1$Class，不能替换原有后缀
                */
                //****************************
                String basePackage = BASE_PACKAGE + packageName + ".";
                CtClass existClass = pool.getOrNull(basePackage + StringUtils.upperFirstChar(actualName));

                if(existClass != null){
                    Object remove = fieldMap.remove(actualName);
                    int seqNum = 1;
                    while(existClass != null){
                        int suffixIndex = actualName.indexOf("$");
                        if(suffixIndex >= 0){
                            String tempName = actualName.substring(0,suffixIndex);
                            tempName += "$"+ seqNum;
                            tempName += actualName.substring(suffixIndex);
                            actualName = tempName;
                            seqNum++;
                        }
                        existClass = pool.getOrNull(basePackage + StringUtils.upperFirstChar(actualName));
                    }
                    fieldMap.put(actualName,remove);
                    if(logger.isDebugEnabled()){
                        logger.debug("类结构被冻结，生成新的中介类："+basePackage + StringUtils.upperFirstChar(actualName));
                    }
                }

//                if(existClass != null){
//                    Object remove = fieldMap.remove(actualName);
//                    int suffixIndex = actualName.indexOf("$");
//                    if(suffixIndex >= 0){
//                        String tempName = actualName.substring(0,suffixIndex);
//                        tempName += "$COPY";
//                        tempName += actualName.substring(suffixIndex);
//                        actualName = tempName;
//                    }
//                    fieldMap.put(actualName,remove);
//                }

                CtClass clazz = pool.makeClass(basePackage + StringUtils.upperFirstChar(actualName));

                Class subClass = createClass(packageName, clazz, (Map) fieldMap.get(actualName),name.endsWith(ARRAY_CLASS_SUFFIX));
                attributes = new HashMap<>();
                attributes.put("dataType",subClass.getName());
                attributes.put("name",actualName);
                attributes.put("value",actualName);
            }else{
                ApiField apiField = (ApiField)fieldMap.get(name);
                attributes = AnnotationUtils.getAnnotationAttributes(apiField);
            }
            CtMethod readMethod = createReadMethod(attributes, parent);
            CtMethod writeMethod = createWriteMethod(attributes, parent);
            try {
                parent.addMethod(readMethod);
                parent.addMethod(writeMethod);
            } catch (CannotCompileException e) {
                throw new RuntimeException("为class添加方法时发生错误",e);
            }
            logger.debug("创建中介类方法:"+parent.getName()+"."+readMethod.getName()+"/"+writeMethod.getName());
        }
        try {
            ClassFile classFile = parent.getClassFile();
            classFileMap.put(parent,classFile);

            Class<?> resultClass = parent.toClass();
            logger.debug("创建中介类:"+parent.getName());
            if(isArray){
                Object array = Array.newInstance(resultClass, 0);
                resultClass = array.getClass();
            }

            return resultClass;
        } catch (CannotCompileException e) {
            throw new RuntimeException("根据字段定义方式生成Swagger文档，编译Class时发生异常，请检查@ApiJsonField注解的name属性是否配置了重复的路径名:" + parent.getName(),e);
        }
    }

    /**
     * 整理所有定义的ApiJsonField注解为一个树形的Map数据格式，层级以属性名"."分隔，如果包含"."，说明只有最后一级为属性，其余层级均为
     * Class，同时处理如果类名部分的名称为"[]"结尾，意为该属性为数组
     * @param fields
     * @return
     */
    private Map<String,Object> formatField(ApiField[] fields){
        Map<String,Object> cachedMap = new LinkedHashMap<>();
        for (int i = 0; i < fields.length; i++) {
            String fieldName = fields[i].name();
            String[] nameSplit = fieldName.split("[.]");
            Map<String,Object> fieldMap = formatSubClassMap(nameSplit,cachedMap);
            fieldMap.put(nameSplit[nameSplit.length - 1],fields[i]);
        }
        return cachedMap;
    }

    private Map<String, Object> formatSubClassMap(String[] nameSplit,Map<String, Object> classMap) {
        Map<String,Object> resultMap = classMap;
        if(nameSplit.length > 1) {
            //修整、去除如果数组的后缀（如果包含）
            String trimName = nameSplit[0];
            trimName = trimName.endsWith("[]")?trimName.substring(0,trimName.length()-2):trimName;

            String classSuffix = nameSplit[0].endsWith("[]") ? ARRAY_CLASS_SUFFIX : CLASS_SUFFIX;
            resultMap = (Map<String, Object>) classMap.get(trimName + classSuffix);
            if(resultMap == null){
                resultMap = new HashMap<>();
                classMap.put(trimName + classSuffix,resultMap);
            }
            String[] temp = new String[nameSplit.length-1];
            System.arraycopy(nameSplit,1,temp,0,temp.length);
            resultMap = formatSubClassMap(temp,resultMap);
        }
        return resultMap;
    }

//    private String newClassName(String className,int seqNum){
//        int suffixIndex = className.indexOf("$");
//        String tempName = className.substring(0,suffixIndex);
//        tempName += "$"+ seqNum;
//        tempName += className.substring(suffixIndex);
//        return tempName;
//    }

    private Class dataType(String dataType) {
        if(org.springframework.util.StringUtils.isEmpty(dataType)) return String.class;

        if("string".equals(dataType.toLowerCase())){
            return String.class;
        }else if("integer".equals(dataType.toLowerCase()) || "int".equals(dataType.toLowerCase())){
            return Integer.class;
        }else if("date".equals(dataType.toLowerCase())){
            return Date.class;
        }else if("double".equals(dataType.toLowerCase()) || "float".equals(dataType.toLowerCase())){
            return Double.class;
        }else if("long".equals(dataType.toLowerCase())){
            return Long.class;
        }else if("bool".equals(dataType.toLowerCase()) || "boolean".equals(dataType.toLowerCase())){
            return Boolean.class;
        }else {
            try {
                return Class.forName(dataType);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("dataType="+dataType,e);
            }
        }
    }


    private CtMethod createReadMethod(Class javaType,String attrName,String remarks,CtClass declaring){
        Map<String, Object> valueMap = ParamMap.create("dataType", javaType.getName()).set("name", attrName).set("value", remarks).toMap();
        return createReadMethod(valueMap,declaring);
    }


    private CtMethod createWriteMethod(Map<String,Object> annoAttrMap,CtClass declaring){
        return createMethod(false,annoAttrMap,declaring);
    }

    private CtMethod createReadMethod(Map<String,Object> annoAttrMap,CtClass declaring){
        return createMethod(true,annoAttrMap,declaring);
    }

    /**
     * 根据给定的属性参数annoAttrMap生成get方法，annoAttrMap中必须包含name，用于get方法的命名
     * @param annoAttrMap 与ApiModelProperty注解一一对应的属性
     * @param declaring
     * @return
     */
    private CtMethod createMethod(boolean readMethod,Map<String,Object> annoAttrMap,CtClass declaring){
        String name = ValueMapUtils.getValueAsString(annoAttrMap, "name");
        String value = ValueMapUtils.getValueAsString(annoAttrMap, "value");
        String allowableValues = ValueMapUtils.getValueAsString(annoAttrMap, "allowableValues");
        String notes = ValueMapUtils.getValueAsString(annoAttrMap, "notes");
        String dataType = ValueMapUtils.getValueAsString(annoAttrMap, "dataType");
        Boolean required = ValueMapUtils.getValueAsBoolean(annoAttrMap, "required");
        String example = ValueMapUtils.getValueAsString(annoAttrMap, "example");
        String reference = ValueMapUtils.getValueAsString(annoAttrMap, "reference");
        Integer position = ValueMapUtils.getValueAsInteger(annoAttrMap, "position");
        Boolean allowMultiple = ValueMapUtils.getValueAsBoolean(annoAttrMap, "allowMultiple");

        Assert.notNull(name,"name属性不得为null");

        int index = name.lastIndexOf(".");
        if(index > -1) name = name.substring(index+1);

        String methodName = StringUtils.upperFirstChar(name);

        Class type = dataType(dataType);

        CtClass typeClass = getCtClass(type);

        AnnotationsAttribute annoAttribute = null;
        //此处的数组判断特指中介数组类，而非最终属性字段。中介类是否是数组，在组装对象树时已经得知，并声明为了数组Class，因此可以在这里判断
        if(!typeClass.isArray()){
            ClassFile classFile = classFileMap.get(typeClass);
            if(classFile == null){
                classFile = typeClass.getClassFile();
                classFileMap.put(typeClass,classFile);
            }

            ConstPool constpool = classFile.getConstPool();
            value = value == null ? name : value;

            annoAttribute = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);

            Annotation apiModelProperty = new Annotation(ApiModelProperty.class.getName(),constpool);

            apiModelProperty.addMemberValue("value",new StringMemberValue(value,constpool));
            if(allowableValues != null) apiModelProperty.addMemberValue("allowableValues",new StringMemberValue(allowableValues,constpool));
            if(notes != null) apiModelProperty.addMemberValue("notes",new StringMemberValue(notes,constpool));
            if(dataType != null) apiModelProperty.addMemberValue("dataType",new StringMemberValue(dataType,constpool));
            if(required != null) apiModelProperty.addMemberValue("required",new BooleanMemberValue(required,constpool));
            if(example != null) apiModelProperty.addMemberValue("example",new StringMemberValue(example,constpool));
            if(reference != null) apiModelProperty.addMemberValue("reference",new StringMemberValue(reference,constpool));
            if(position != null) apiModelProperty.addMemberValue("position",new IntegerMemberValue(constpool,position));
            if(allowMultiple != null) apiModelProperty.addMemberValue("allowMultiple",new BooleanMemberValue(allowMultiple,constpool));

            annoAttribute.addAnnotation(apiModelProperty);
        }


        try {
            //去除Class类的get方法名的后缀，便于生成方法名使用
//            if(methodName.endsWith(CLASS_SUFFIX)){
//                methodName = methodName.substring(0,methodName.length()-CLASS_SUFFIX.length());
//            }else if(methodName.endsWith(ARRAY_CLASS_SUFFIX)){
//                methodName = methodName.substring(0,methodName.length()-ARRAY_CLASS_SUFFIX.length());
//            }
            int classUffix = methodName.indexOf("$");
            if(classUffix >=0){
                methodName = methodName.substring(0,classUffix);
            }

            //判断属性是否是数组类型，即allowMultiple==true
            if(Boolean.valueOf(allowMultiple)){
                Class arrayType = Array.newInstance(type,0).getClass();
                typeClass = getCtClass(arrayType);
            }

            CtMethod method;
            if(readMethod){
                method = new CtMethod(typeClass,"get" + methodName,new CtClass[0],declaring);
                method.setBody("{return null;}");
            }else{
                method = new CtMethod(CtPrimitiveType.voidType,"set" + methodName,new CtClass[]{typeClass},declaring);
                method.setBody("{}");
            }

            MethodInfo methodInfo = method.getMethodInfo();
            if(annoAttribute != null){
                methodInfo.addAttribute(annoAttribute);
            }
            return method;
        } catch (CannotCompileException e) {
            throw new RuntimeException("生成getter方法错误：class=" + declaring.getName(),e);
        }
    }

    /**
     * 得到完整序列包名，以"."结尾
     * @return
     */
    private String getSerialPackageFullName(){
        if(allowNameRepeat){
            return BASE_PACKAGE + getSerialPackageName() + ".";
        }
        return BASE_PACKAGE;
    }

    /**
     * 得到序列包名，不以"."结尾
     * @return
     */
    private String getSerialPackageName(){
        return "pack" + (serial++);
    }

    /**
     * 根据名称创建中介类
     * 负责创建中介类，并处理如果类结构被冻结时，提示友好的错误信息便于定位问题。
     * @param name
     * @return
     */
    private CtClass getCtClass(String name) {
        CtClass ctClass;
        String className = getSerialPackageFullName() + name;
        ClassPool pool = ClassPool.getDefault();
        try{
            ctClass = pool.makeClass(className);
        }catch (RuntimeException e){
            if(e.getMessage().indexOf("frozen class") >= 0){
                String aipType = "ApiJsonResponse";
                if(name.endsWith("Req")){
                    aipType = "ApiJsonRequest";
                }
                throw new RuntimeException("中介类名重复：" + name.substring(0,name.length()-3) +
                        "，请通过@" + aipType + "注解中的name属性来定义一个新的名称。",e);
            }else{
                throw e;
            }
        }
        return ctClass;
    }

    /**
     * 根据类型创建中介类
     * @param javaType
     * @return
     */
    private CtClass getCtClass(Class javaType) {

        ClassPool pool = ClassPool.getDefault();

        CtClass typeClass = pool.getOrNull(javaType.getName());
        if (typeClass == null) {
            typeClass = pool.makeClass(javaType.getName());
        }
        return typeClass;
    }

    @Override
    public boolean supports(DocumentationType delimiter) {
        return true;
    }

}
