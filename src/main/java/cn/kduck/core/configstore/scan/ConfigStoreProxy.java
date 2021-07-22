package cn.kduck.core.configstore.scan;

import cn.kduck.core.configstore.ConfigStoreReloader;
import cn.kduck.core.configstore.annotation.ConfigObject;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;

public class ConfigStoreProxy implements FactoryBean, ApplicationContextAware {

    private final Class configClass;
    private Object configObject;

    private ApplicationContext applicationContext;

    public ConfigStoreProxy(Class configClass){
        this.configClass = configClass;
    }

    @Override
    public Object getObject() throws Exception {
        if(configObject == null){
            ConfigObject annotation = AnnotationUtils.findAnnotation(configClass, ConfigObject.class);

            String configCode = "".equals(annotation.name()) ? configClass.getSimpleName() : annotation.name();

            configObject = processConfigStore(configCode,configClass);
//            AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();
//            beanFactory.autowireBeanProperties(configObject, AutowireCapableBeanFactory.AUTOWIRE_NO, false);
        }
        return configObject;
    }

    private Object processConfigStore(String configCode,Class configClass) throws Exception{
        Object configObject = configClass.newInstance();
        ConfigStoreReloader configStoreReloader = applicationContext.getBean(ConfigStoreReloader.class);
        return configStoreReloader.reloadValue(configCode,configObject);
    }

    @Override
    public Class<?> getObjectType() {
        return configClass;
    }

    public Class getConfigClass() {
        return configClass;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
