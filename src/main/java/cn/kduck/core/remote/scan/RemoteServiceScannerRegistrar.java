package cn.kduck.core.remote.scan;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.AnnotationMetadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RemoteServiceScannerRegistrar implements BeanFactoryAware, ImportBeanDefinitionRegistrar {

    private final Log logger = LogFactory.getLog(getClass());

    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        List<String> packages = getScanPackages();
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(RemoteServiceScannerConfigurer.class);
            builder.addPropertyValue("basePackage", packages);
//            BeanWrapper beanWrapper = new BeanWrapperImpl(RemoteServiceScannerConfigurer.class);
//            Stream.of(beanWrapper.getPropertyDescriptors()).filter((x) -> {
//                return x.getName().equals("lazyInitialization");
//            }).findAny().ifPresent((x) -> {
//                builder.addPropertyValue("lazyInitialization", "${mybatis.lazy-initialization:false}");
//            });
            registry.registerBeanDefinition(RemoteServiceScannerConfigurer.class.getName(), builder.getBeanDefinition());
    }



    private List<String> getScanPackages() {
//        List<String> packages = AutoConfigurationPackages.get(this.beanFactory);
        List<String> packages = new ArrayList<>();
        Class<?> mainClass = deduceMainApplicationClass();
        packages.add(mainClass.getPackage().getName());
        if(mainClass != null) {
            SpringBootApplication springBoot = AnnotationUtils.findAnnotation(mainClass, SpringBootApplication.class);
            if(springBoot != null){
                packages.addAll(Arrays.asList(springBoot.scanBasePackages()));
            }

            ComponentScan componentScan = AnnotationUtils.findAnnotation(mainClass, ComponentScan.class);
            if(componentScan != null){
                packages.addAll(Arrays.asList(componentScan.basePackages()));
            }
            ComponentScans componentScans = AnnotationUtils.findAnnotation(mainClass, ComponentScans.class);
            if(componentScans != null){
                ComponentScan[] scans = componentScans.value();
                for (ComponentScan scan : scans) {
                    packages.addAll(Arrays.asList(scan.basePackages()));
                }
            }
            packages = reorganizePackages(packages);
        }

        return packages;
    }

    private List<String> reorganizePackages(List<String> packages){
        List<String> reformatList = new ArrayList();

        for (String p : packages) {
            boolean skip = false;
            for (String fp : reformatList) {
                if(fp.startsWith(p)){
                    reformatList.remove(fp);
                    break;
                }else if(p.startsWith(fp)){
                    skip = true;
                    break;
                }
            }
            if(!skip){
                reformatList.add(p);
            }
        }

        return reformatList;
    }

    private Class<?> deduceMainApplicationClass() {
        try {
            StackTraceElement[] stackTrace = new RuntimeException().getStackTrace();
            for (StackTraceElement stackTraceElement : stackTrace) {
                if ("main".equals(stackTraceElement.getMethodName())) {
                    return Class.forName(stackTraceElement.getClassName());
                }
            }
        }
        catch (ClassNotFoundException ex) {
            // Swallow and continue
        }
        return null;
    }
}
