package cn.kduck.core.configstore.scan;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;

import java.util.Set;

public class ConfigStoreScanner extends ClassPathBeanDefinitionScanner {

    public ConfigStoreScanner(BeanDefinitionRegistry registry){
        super(registry, false);
    }

    @Override
    public Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);

        if (!beanDefinitions.isEmpty()) {
            processBeanDefinitions(beanDefinitions);
        }

        return beanDefinitions;
    }

    private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {
        GenericBeanDefinition definition;
        for (BeanDefinitionHolder holder : beanDefinitions) {
            definition = (GenericBeanDefinition) holder.getBeanDefinition();
            Class<?> defClass = null;
            try {
                defClass = Class.forName(definition.getBeanClassName());
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            processConfigStoreDefinitions(definition,defClass);
//            if(isConfigStore(defClass)){
//                processConfigStoreDefinitions(definition,defClass);
//            }
        }
    }

    private void processConfigStoreDefinitions(GenericBeanDefinition definition, Class<?> configClass) {
        definition.getConstructorArgumentValues().addGenericArgumentValue(configClass);
        definition.getConstructorArgumentValues().addGenericArgumentValue(new RuntimeBeanReference("configStoreReloader"));
        definition.setBeanClass(ConfigStoreProxy.class);
    }

//    private boolean isConfigStore(Class<?> defClass) {
//        return AnnotationUtils.findAnnotation(defClass, ConfigObject.class) != null;
//    }
}
