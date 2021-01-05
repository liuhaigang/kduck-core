package cn.kduck.core.dao.query;

import cn.kduck.core.dao.definition.BeanDefDepository;
import cn.kduck.core.service.exception.QueryNotFoundException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

/**
 * LiuHG
 */
@Component
public class QueryFactory implements ApplicationContextAware {

    @Autowired
    private BeanDefDepository depository;

    @Autowired(required = false)
    private List<QueryCreator> queryCreatorList;
    private ApplicationContext applicationContext;


    public QuerySupport getQuery(String name, Map<String,Object> paramMap){
        Assert.notNull(name,"获取QueryCreator的名称不能为null");
        Assert.notNull(queryCreatorList,"获取Query失败，当前系统中不存在任何QueryCreator对象，请确认你的QueryCreator实现类是否声明为了Spring的Bean："+name);
        for (QueryCreator queryCreator : queryCreatorList) {
            if(name.equals(queryCreator.queryCode())){
                QuerySupport query = queryCreator.createQuery(paramMap, depository);
                if(query instanceof CustomQueryBean){
                    ((CustomQueryBean)query).setGenerateBy(queryCreator.getClass().getSimpleName());
                }
                return query;
            }
        }
        throw new QueryNotFoundException("没有找到名称为" + name + "的QueryCreator");
    }

    public QuerySupport getQuery(Class<? extends QueryCreator> className, Map<String,Object> paramMap){
        QueryCreator queryCreator;
        try{
            queryCreator = applicationContext.getBean(className);
        }catch (NoSuchBeanDefinitionException e){
            throw new QueryNotFoundException("没有找到Class为" + className + "的QueryCreator",e);
        }
        QuerySupport query = queryCreator.createQuery(paramMap, depository);
        if(query instanceof CustomQueryBean){
            ((CustomQueryBean)query).setGenerateBy(queryCreator.getClass().getSimpleName());
        }
        return query;

    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
