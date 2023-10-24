package cn.kduck.core.dao.query;

import cn.kduck.core.dao.definition.BeanDefDepository;
import cn.kduck.core.service.exception.QueryNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LiuHG
 */
@Component
public class QueryFactory {

    private BeanDefDepository depository;

    private List<QueryCreator> queryCreatorList;

    public QueryFactory(BeanDefDepository depository,@Autowired(required = false) List<QueryCreator> queryCreatorList){
        this.depository = depository;
        this.queryCreatorList = queryCreatorList;
    }

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
        QueryCreator queryCreator = null;
            for (QueryCreator creator : queryCreatorList) {
                if(creator.getClass() == className){
                    queryCreator = creator;
                    break;
                }
            }
        if(queryCreator == null){
            throw new RuntimeException("没有找到Class为" + className + "的QueryCreator");
        }
        QuerySupport query = queryCreator.createQuery(new HashMap(paramMap), depository);
        if(query instanceof CustomQueryBean){
            ((CustomQueryBean)query).setGenerateBy(queryCreator.getClass().getSimpleName());
        }
        return query;

    }

}
