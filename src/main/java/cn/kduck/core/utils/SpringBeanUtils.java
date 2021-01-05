package cn.kduck.core.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author LiuHG
 */
@Component
public final class SpringBeanUtils implements ApplicationContextAware {

    private static ApplicationContext cxt;

    private SpringBeanUtils(){}

    public static <B> B getBean(String id) {
        return (B)getApplicationContext().getBean(id);
    }

    public static <B> B getBean(Class<B> requiredType) {
        return (B)getApplicationContext().getBean(requiredType);
    }

    private static ApplicationContext getApplicationContext(){
        if(cxt == null){
            throw new RuntimeException("当前对象未实例化，请不要在Spring的Bean中声明全局变量以SpringBeanUtils.getBean(String)方式获取实例，可考虑使用注入方式。");
        }
        return cxt;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.cxt = applicationContext;
    }
}
