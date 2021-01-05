package cn.kduck.core.configstore.scan;

import cn.kduck.core.utils.ScanPackageUtils;
import cn.kduck.core.configstore.annotation.ConfigObject;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.StringUtils;

import java.util.List;

//@ConditionalOnBean(ConfigStoreReloader.class)
public class ConfigStoreScannerConfigurer implements BeanDefinitionRegistryPostProcessor, ApplicationContextAware, BeanFactoryAware {
    private ApplicationContext applicationContext;
    private BeanFactory beanFactory;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        ConfigStoreScanner scanner = new ConfigStoreScanner(registry);
        scanner.setResourceLoader(this.applicationContext);
        scanner.addIncludeFilter(new AnnotationTypeFilter(ConfigObject.class));
        scanner.setBeanNameGenerator((BeanDefinition beanDefinition, BeanDefinitionRegistry beanRegistry)->{
            Class<?> configStoreClass;
            try {
                configStoreClass = Class.forName(beanDefinition.getBeanClassName());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("class not found",e);
            }
            ConfigObject annotation = AnnotationUtils.findAnnotation(configStoreClass, ConfigObject.class);
            String beanName = StringUtils.hasText(annotation.name()) ? annotation.name() : configStoreClass.getSimpleName();
            return beanName;
        });
        List<String> packages = ScanPackageUtils.getScanPackages(this.beanFactory);
        scanner.scan(StringUtils.toStringArray(packages));
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
}
