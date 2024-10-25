package cn.kduck.core.dao.definition;

import cn.kduck.core.dao.datasource.DataSourceSwitch;
import cn.kduck.core.dao.exception.EntityDefNotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.util.*;

/**
 * LiuHG
 */
public class MemoryBeanDefDepository implements BeanDefDepository,InitializingBean {

    private final Log logger = LogFactory.getLog(getClass());

    public List<BeanDefSource> beanDefSourceList;

    private TableAliasGenerator tableAliasGenerator = new DefaultTableAliasGenerator();

    private Map<String,BeanEntityDef> beanEntityDefMap =  new HashMap<>();//存储所有实体信息的Map对象

    public MemoryBeanDefDepository(List<BeanDefSource> beanDefSourceList){
        this.beanDefSourceList = beanDefSourceList;
    }

    public void reloadAllEntity(){
        if(logger.isInfoEnabled()){
            logger.info("BeanDefSource:"+ Arrays.toString(beanDefSourceList.toArray()));
        }

        for (BeanDefSource source : beanDefSourceList) {
            List<BeanEntityDef> beanEntityDefs = source.listEntityDef();
            Assert.notNull(beanEntityDefs,"实体定义源返回的实体定义集合不能为null：" + source.getClass());
            for (BeanEntityDef entityDef : beanEntityDefs) {
                String entityCode = entityDef.getEntityCode().toUpperCase();
                if(!beanEntityDefMap.containsKey(entityCode)){
                    beanEntityDefMap.put(entityCode,entityDef);
                }
            }
        }
    }

    public BeanEntityDef getEntityDef(String name){
        name = formatEntityCode(name);
        BeanEntityDef entityDef = beanEntityDefMap.get(name.toUpperCase());
        if(entityDef == null){
            throw new EntityDefNotFoundException("指定名称的实体定义对象不存在：" + name);
        }

        return entityDef;
    }

    public final List<BeanFieldDef> getFieldDefList(String entityDefName){
        BeanEntityDef entityDef = getEntityDef(entityDefName);
        return entityDef.getFieldList();
    }

    @Override
    public void deleteEntity(String name) {
        name = formatEntityCode(name);
        beanEntityDefMap.remove(name.toUpperCase());
    }

    //TODO 暂不能处理刷新删除的实体
    @Override
    public void reloadEntity(String tableName) {
        tableName = formatEntityCode(tableName);
        String entityCode = tableAliasGenerator.genAlias(tableName);
        for (BeanDefSource source : beanDefSourceList) {
            BeanEntityDef beanEntityDef1 = source.reloadEntity(tableName);
            if(beanEntityDef1 == null) continue;
            beanEntityDefMap.put(entityCode,beanEntityDef1);

            for(BeanEntityDef beanEntityDef  : beanEntityDefMap.values()){
                BeanEntityDef[] fkBeanEntityDef = beanEntityDef.getFkBeanEntityDef();
                if(fkBeanEntityDef != null){
                    for (int j = 0; j < fkBeanEntityDef.length; j++) {
                        if(fkBeanEntityDef[j].getEntityCode().equals(beanEntityDef1.getEntityCode())){
                            fkBeanEntityDef[j] = beanEntityDef1;
                            break;
                        }
                    }
                }
            }
            if(logger.isInfoEnabled()){
                logger.info("实体信息已被刷新：" + entityCode + ",BeanDefSource:" + source.getClass().getName());
            }
        }
    }

    @Override
    public Map<String, BeanEntityDef> getAllEntityDef() {
        return Collections.unmodifiableMap(beanEntityDefMap);
    }

    private String formatEntityCode(String code){
        if(DataSourceSwitch.isEnabled()) {
            String dsKey = DataSourceSwitch.get();
            if(dsKey == null){
                dsKey = DataSourceSwitch.getLookupKeys()[0];
            }
            return dsKey + "." + code;
        }else{
            return code;
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        reloadAllEntity();
    }
}
