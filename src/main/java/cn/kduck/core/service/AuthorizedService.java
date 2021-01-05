package cn.kduck.core.service;

import cn.kduck.core.dao.definition.BeanEntityDef;
import cn.kduck.core.dao.definition.BeanFieldDef;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 字段进行授权字端过滤
 * //TODO 未实现完全
 * LiuHG
 */
public class AuthorizedService extends DefaultService {

    @Autowired(required = false)
    private AuthorizedFieldFilter filter;


    private void listAuthorizedField(Map<String, Object> valueMap,String user, BeanEntityDef entityDef){
        if(filter == null) return;

        List<String> fields = filter.doFieldFilter(user, entityDef);
        Iterator<String> allKeys = valueMap.keySet().iterator();

        List<String> noRightFieldList = new ArrayList();

        BeanFieldDef pkFieldDef = entityDef.getPkFieldDef();
        String pkName = null;
        if(pkFieldDef != null){
            pkName = pkFieldDef.getAttrName();
        }

        while(allKeys.hasNext()){
            String fName = allKeys.next();
            //如果是主键属性，则不控制权限
            if(fName.equals(pkName))continue;

            if(!fields.contains(fName)){
                noRightFieldList.add(fName);
            }
        }

        for (String fName : noRightFieldList) {
            valueMap.remove(fName);
        }

    }

    protected void fieldFilter(ValueBean valueBean) {
        BeanEntityDef entityDef = valueBean.getEntityDef();
        Map<String, Object> valueMap = valueBean.getValueMap();
        listAuthorizedField(valueMap,"",entityDef);
    }

}
