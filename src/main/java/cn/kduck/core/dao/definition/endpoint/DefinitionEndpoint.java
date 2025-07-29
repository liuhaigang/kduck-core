package cn.kduck.core.dao.definition.endpoint;

import cn.kduck.core.dao.definition.BeanDefDepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;

import java.util.Set;

@WebEndpoint(id = "definition")
public class DefinitionEndpoint {

    @Autowired
    private BeanDefDepository beanDefDepository;

    @ReadOperation
    public String[] listDefinitionObject() {
        Set<String> entityCodeSet = beanDefDepository.getAllEntityDef().keySet();

        return entityCodeSet.toArray(new String[0]);
    }

    @ReadOperation
    public Object getDefinitionObject(@Selector String entityCode) {
        return beanDefDepository.getEntityDef(entityCode);
    }
}
