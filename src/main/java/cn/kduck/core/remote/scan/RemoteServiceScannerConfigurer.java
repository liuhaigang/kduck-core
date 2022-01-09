package cn.kduck.core.remote.scan;

import cn.kduck.core.remote.annotation.ProxyService;
import cn.kduck.core.utils.ScanPackageUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class RemoteServiceScannerConfigurer implements BeanDefinitionRegistryPostProcessor, ApplicationContextAware, BeanFactoryAware {

    private final Log logger = LogFactory.getLog(getClass());

    private ApplicationContext applicationContext;
    private BeanFactory beanFactory;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        RemoteServiceScanner scanner = new RemoteServiceScanner(registry);
        scanner.setResourceLoader(this.applicationContext);
        scanner.addIncludeFilter(new AnnotationTypeFilter(ProxyService.class));
        scanner.setBeanNameGenerator(new DefaultBeanNameGenerator());
        List<String> packages = ScanPackageUtils.getScanPackages(this.beanFactory);

        if(logger.isInfoEnabled()){
            logger.info("Scan proxy service packages:" + Arrays.toString(packages.toArray()));
        }


        packages.add("cn.kduck");

        scanner.scan(StringUtils.toStringArray(reorganizePackages(packages)));
    }

//    private List<String> getScanPackages() {
//        List<String> packages = AutoConfigurationPackages.get(this.beanFactory);
//        Class<?> mainClass = deduceMainApplicationClass();
//        if(mainClass != null) {
//            SpringBootApplication springBoot = AnnotationUtils.findAnnotation(mainClass, SpringBootApplication.class);
//            if(springBoot != null){
//                packages.addAll(Arrays.asList(springBoot.scanBasePackages()));
//            }
//
//            ComponentScan componentScan = AnnotationUtils.findAnnotation(mainClass, ComponentScan.class);
//            if(componentScan != null){
//                packages.addAll(Arrays.asList(componentScan.basePackages()));
//            }
//            ComponentScans componentScans = AnnotationUtils.findAnnotation(mainClass, ComponentScans.class);
//            if(componentScans != null){
//                ComponentScan[] scans = componentScans.value();
//                for (ComponentScan scan : scans) {
//                    packages.addAll(Arrays.asList(scan.basePackages()));
//                }
//            }
//            packages = reorganizePackages(packages);
//        }
//
//        return packages;
//    }

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


    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

//    private Class<?> deduceMainApplicationClass() {
//        try {
//            StackTraceElement[] stackTrace = new RuntimeException().getStackTrace();
//            for (StackTraceElement stackTraceElement : stackTrace) {
//                if ("main".equals(stackTraceElement.getMethodName())) {
//                    return Class.forName(stackTraceElement.getClassName());
//                }
//            }
//        }
//        catch (ClassNotFoundException ex) {
//            // Swallow and continue
//        }
//        return null;
//    }
}
