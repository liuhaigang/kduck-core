package cn.kduck.core.web.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cn.kduck.core.utils.PathUtils;
import cn.kduck.core.web.annotation.ModelOperate;
import cn.kduck.core.web.annotation.ModelResource;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.DigestUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * LiuHG
 */
public class ModelResourceLoader implements InitializingBean, BeanFactoryAware {

    private static final Log logger = LogFactory.getLog(ModelResourceLoader.class);

    private String RESOURCE_PATTERN = "/**/*.class";

    private ObjectMapper om = new ObjectMapper();

    @Value("${kduck.resource.basePackage:}")
    private String[] packagesToScan;

    private final List<ModelResourceProcessor> resourceProcessorList;
    private BeanFactory beanFactory;

    public ModelResourceLoader(ObjectProvider<ModelResourceProcessor> resourceProcessorProvider){
        this.resourceProcessorList = Collections.unmodifiableList(new ArrayList<>(resourceProcessorProvider.stream().collect(Collectors.toList())));;
    }

    private void load(){
        if(packagesToScan == null || packagesToScan.length == 0){
            List<String> packages = AutoConfigurationPackages.get(this.beanFactory);
            packagesToScan = packages.toArray(new String[0]);
            if(logger.isInfoEnabled()){
                logger.info("未配置资源类扫描路径（kduck.resource.basePackage），使用默认路径：" + Arrays.toString(packagesToScan));
            }
        }

        int total = 0;
        if(logger.isInfoEnabled()){
            logger.info("准备扫描资源："+ Arrays.toString(packagesToScan));
        }

        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        for (String pkg : packagesToScan) {
            String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                    + ClassUtils.convertClassNameToResourcePath(pkg) + RESOURCE_PATTERN;
            Resource[] resources;
            try {
                resources = resolver.getResources(pattern);
            } catch (IOException e) {
                throw new RuntimeException("获取资源文件错误："+pattern,e);
            }

            MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(resolver);
            for (Resource resource : resources) {
                MetadataReader reader;
                try {
                    reader = readerFactory.getMetadataReader(resource);
                } catch (IOException e) {
                    throw new RuntimeException("获取资源元数据错误：" + resource,e);
                }
                if (resource.isReadable()) {
                    ClassMetadata classMetadata = reader.getClassMetadata();
                    if(classMetadata.isAbstract() || classMetadata.isInterface()){
                        continue;
                    }

                    Class<?> clazz;
                    try {
                        clazz = Class.forName(classMetadata.getClassName(),false,this.getClass().getClassLoader());
                    } catch (Throwable e) {
                        //TODO i18n
                        logger.debug("类不存在或无法实例化（比如依赖的import类文件不存在）："+classMetadata.getClassName(), e);
                        continue;
                    }
                    ResourceValueMap r = processResource(clazz);

                    if(r != null){
                        total++;
                        String signature = makeSignature(r);
                        r.setMd5(signature);
                        for (ModelResourceProcessor resourceProcessor : resourceProcessorList) {
                            resourceProcessor.doProcess(r,clazz);
                        }
                        //查询数据库中是否有该md5的值
                        //如没有，查询数据库里是否有对应code的资源，对资源先进行废除状态，然后再根据code更新资源值及操作值
                    }
                }
            }
        }

        if(logger.isInfoEnabled()){
            logger.info("资源扫描完成，资源数量：" + total);
        }
    }

    /**
     * 生成资源对象的唯一签名标识，此标识用于系统重启再次扫描时对资源进行存在性检测
     * @param resourceValueMap
     * @return 签名
     */
    protected String makeSignature(ResourceValueMap resourceValueMap)  {
        String json;
        try {
            json = om.writeValueAsString(resourceValueMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("为资源生成签名错误：" + resourceValueMap.getResourceName() + " - " + resourceValueMap.getResourceCode(),e);
        }
        return DigestUtils.md5DigestAsHex(json.getBytes());
    }

    /**
     * 处理单个需要分析的资源类对象
     * @param clazz
     * @return
     */
    private ResourceValueMap processResource(Class<?> clazz) {
        ModelResource resourceAnno = AnnotationUtils.findAnnotation(clazz, ModelResource.class);
        if(resourceAnno != null){
            RequestMapping requestMappingAnno = AnnotationUtils.findAnnotation(clazz,RequestMapping.class);
            String[] paths = requestMappingAnno.value();

            ResourceValueMap resource = new ResourceValueMap();

            if(StringUtils.hasText(resourceAnno.value())){
                resource.setResourceName(resourceAnno.value());
            } else {
                //如果swagger注解存在，则尝试从swagger注解中获取模块名称
                Api swaggerApi = AnnotationUtils.findAnnotation(clazz, Api.class);
                if(swaggerApi != null){
                    String moduleName = StringUtils.hasText(swaggerApi.tags()[0]) ? swaggerApi.tags()[0] : swaggerApi.value();
                    resource.setResourceName(moduleName);
                }
                if(!StringUtils.hasText(resource.getResourceName())){
                    throw new IllegalArgumentException("标记@ModelResource注解必须指定模块名称：" + clazz.getName());
                }
            }


            String resCode = StringUtils.isEmpty(resourceAnno.code()) ? clazz.getName() : resourceAnno.code();
            resource.setResourceCode(resCode);

            List<OperateValueMap> resourceOperates = null;
            if(paths.length > 0){
                for (String basePath : paths) {
                    resourceOperates = processOperate(clazz, basePath);
                }
            }else{
                resourceOperates = processOperate(clazz, null);
            }
            Collections.sort(resourceOperates, Comparator.comparing((o)->o.getValueAsString("operateCode")));
            resource.setOperateList(resourceOperates);

            if(logger.isInfoEnabled()){
                logger.info("扫描资源 - " + clazz.getName() + "：资源名：" + resource.getResourceName()+"，操作数量：" + resource.getOperateList().size());
            }

            return resource;
        }
        return null;
    }

    /**
     * 处理资源操作
     * @param clazz
     * @param basePath
     * @return
     */
    private List<OperateValueMap> processOperate(Class<?> clazz, String basePath) {
        List<OperateValueMap> operateList = new ArrayList<>();
        Method[] allDeclaredMethods = ReflectionUtils.getAllDeclaredMethods(clazz);
        for (Method method : allDeclaredMethods) {
            ModelOperate optAnno = AnnotationUtils.findAnnotation(method, ModelOperate.class);
            if(optAnno != null){
                Annotation[] annotations = AnnotationUtils.getAnnotations(method);
                for(Annotation anno : annotations){
                    if(anno instanceof RequestMapping ||
                            anno instanceof PostMapping ||
                            anno instanceof GetMapping ||
                            anno instanceof PutMapping ||
                            anno instanceof DeleteMapping){
                        String[] methodPaths = (String[]) AnnotationUtils.getValue(anno, "path");

                        RequestMapping requestMapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);
                        RequestMethod[] methods = requestMapping.method();
                        String m = methods.length == 0 ? "*" : methods[0].name();

                        if(methodPaths.length == 0){
//                            OperateValueMap resOpt = new OperateValueMap();
//
//                            if(StringUtils.hasText(optAnno.name())){
//                                resOpt.setOperateName(optAnno.name());
//                            }else{
//                                ApiOperation swaggerOperation = AnnotationUtils.findAnnotation(method, ApiOperation.class);
//                                if(swaggerOperation != null){
//                                    String operationName = StringUtils.hasText(swaggerOperation.tags()[0]) ? swaggerOperation.tags()[0] : swaggerOperation.value();
//                                    resOpt.setOperateName(operationName);
//                                }
//                                if(!StringUtils.hasText(resOpt.getOperateName())){
//                                    throw new IllegalArgumentException("标记@ModelOperate注解必须指定操作名称：" + clazz.getName());
//                                }
//                            }
//
//
//                            String optCode = StringUtils.isEmpty(optAnno.code()) ? method.getName() : optAnno.code();
//                            resOpt.setOperateCode(optCode);
//                            resOpt.setOperatePath(PathUtils.appendPath(basePath,null));
//                            resOpt.setGroupCode(optAnno.group());
                            OperateValueMap resOpt = getOperateValueMap(PathUtils.appendPath(basePath,null),optAnno,method);
                            resOpt.setMethod(m);
                            checkOperate(resOpt.getOperateCode(),operateList);
                            operateList.add(resOpt);
                        }else{
                            //FIXME path more than one
                            for (int i = 0; i < methodPaths.length; i++) {
//                                OperateValueMap resOpt = new OperateValueMap();
//
//                                if(StringUtils.hasText(optAnno.name())){
//                                    resOpt.setOperateName(optAnno.name());
//                                }else{
//                                    ApiOperation swaggerOperation = AnnotationUtils.findAnnotation(method, ApiOperation.class);
//                                    if(swaggerOperation != null){
//                                        String operationName = StringUtils.hasText(swaggerOperation.tags()[0]) ? swaggerOperation.tags()[0] : swaggerOperation.value();
//                                        resOpt.setOperateName(operationName);
//                                    }
//                                    if(!StringUtils.hasText(resOpt.getOperateName())){
//                                        throw new IllegalArgumentException("标记@ModelOperate注解必须指定操作名称：" + clazz.getName());
//                                    }
//                                }
//
//                                String optCode = StringUtils.isEmpty(optAnno.code()) ? method.getName() : optAnno.code();
//                                resOpt.setOperateCode(optCode);
//                                resOpt.setOperatePath(PathUtils.appendPath(basePath,methodPaths[i]));
//                                resOpt.setGroupCode(optAnno.group());
                                OperateValueMap resOpt = getOperateValueMap(PathUtils.appendPath(basePath,methodPaths[i]),optAnno,method);
                                resOpt.setMethod(m);
                                checkOperate(resOpt.getOperateCode(),operateList);
                                operateList.add(resOpt);
                            }
                        }
                        break;
                    }
                }
            }
        }

        return operateList;
    }

    private void checkOperate (String code,List<OperateValueMap> operateList){
        for (OperateValueMap operateMap : operateList) {
            if(operateMap.containsKey(code)) {
                throw new RuntimeException("资源操作编码已经存在：" + code);
            }
        }
    }

    private OperateValueMap getOperateValueMap(String operatePath,ModelOperate optAnno,Method method){
        OperateValueMap resOpt = new OperateValueMap();

        if(StringUtils.hasText(optAnno.name())){
            resOpt.setOperateName(optAnno.name());
        }else{
            ApiOperation swaggerOperation = AnnotationUtils.findAnnotation(method, ApiOperation.class);
            if(swaggerOperation != null && StringUtils.hasText(swaggerOperation.value())){
                resOpt.setOperateName(swaggerOperation.value());
            }
            if(!StringUtils.hasText(resOpt.getOperateName())){
                throw new IllegalArgumentException("标记@ModelOperate注解必须指定操作名称：class=" + method.getDeclaringClass().getName() + ",method=" + method.getName());
            }
        }

        String optCode = StringUtils.isEmpty(optAnno.code()) ? method.getName() : optAnno.code();
        resOpt.setOperateCode(optCode);
        resOpt.setOperatePath(operatePath);
        resOpt.setGroupCode(optAnno.group());
//        resOpt.setMethod(m);
        return resOpt;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        load();
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
