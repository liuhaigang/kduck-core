package cn.kduck.core.remote.scan;

import cn.kduck.core.remote.service.RemoteServiceProxy;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;

import java.util.Set;

public class RemoteServiceScanner extends ClassPathBeanDefinitionScanner {

    private final BeanDefinitionRegistry registry;

    public RemoteServiceScanner(BeanDefinitionRegistry registry){
        super(registry, false);
        this.registry = registry;
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
        }
    }

    private void processConfigStoreDefinitions(GenericBeanDefinition definition, Class<?> remoteService) {
//        String beanName = BeanDefinitionReaderUtils.generateBeanName(definition, registry);
//        definition.getConstructorArgumentValues().addGenericArgumentValue(new RuntimeBeanReference("userServiceImpl1"));
        definition.getConstructorArgumentValues().addGenericArgumentValue(remoteService);
        definition.setBeanClass(RemoteServiceProxy.class);
        definition.setPrimary(true);
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
    }

}
