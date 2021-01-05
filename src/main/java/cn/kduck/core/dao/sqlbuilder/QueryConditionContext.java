package cn.kduck.core.dao.sqlbuilder;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public final class QueryConditionContext {

    private static List<QueryConditionDefiner> definerList;

    //for junit test
    public QueryConditionContext(List<QueryConditionDefiner> definerLis){
        this.definerList = definerLis;
    }

    @Autowired
    public QueryConditionContext(ObjectProvider<QueryConditionDefiner> objectProvider){
        this.definerList = Collections.unmodifiableList(new ArrayList<>(objectProvider.stream().collect(Collectors.toList())));
    }

    public static List<QueryConditionDefiner> getConditionDefiner(){
        if(definerList == null){
            return Collections.emptyList();
        }
        return definerList;
    }

}
