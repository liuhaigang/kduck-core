package cn.kduck.core.dao.definition.impl;

import cn.kduck.core.KduckProperties;
import cn.kduck.core.KduckProperties.EntityDefinitionProperties;
import cn.kduck.core.KduckProperties.ScanTablesProperties;
import cn.kduck.core.dao.datasource.DataSourceSwitch;
import cn.kduck.core.dao.datasource.DynamicDataSource;
import cn.kduck.core.dao.definition.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * LiuHG
 */
public class JdbcBeanDefSource implements BeanDefSource {

    private final Log logger = LogFactory.getLog(JdbcBeanDefSource.class);

    private DataSource dataSource;

    private FieldAliasGenerator fieldAliasGenerator = new DefaultFieldAliasGenerator();

    private TableAliasGenerator tableAliasGenerator = new DefaultTableAliasGenerator();

    private FieldDefCorrector fieldDefCorrector;
    private EntityDefCorrector entityDefCorrector;

    private EntityDefinitionProperties definitionProperties;

    public JdbcBeanDefSource(DataSource dataSource, EntityDefinitionProperties definitionProperties){
        this.dataSource = dataSource;
        this.definitionProperties = definitionProperties;
    }

    @Override
    public String getNamespace() {
        return getClass().getName();
    }

    private boolean analysisTable(String tableName){
        ScanTablesProperties tables = definitionProperties.getTables();
        String[] includeTables = tables.getInclude();
        String[] excludeTables = tables.getExclude();
        if(includeTables.length == 0 && excludeTables.length == 0 ) return true;

        //Kduck表默认都需要扫描
//        if(tableName.startsWith("K_")) return true;

        if (match(tableName,includeTables)) return true;

        if(includeTables.length == 0 && !match(tableName,excludeTables)) return true;
        return false;
    }

    private boolean match(String tableName,String[] tables) {
        tableName = tableName.toUpperCase();
        for (String tablePattern : tables) {
            tablePattern = tablePattern.toUpperCase();
            if(tablePattern.endsWith("*")){
                String prefix = tablePattern.substring(0, tablePattern.length() - 1);
                if(tableName.startsWith(prefix)){
                    return true;
                }
            }else if(tableName.equals(tablePattern)){
                return true;
            }
        }
        return false;
    }

    @Override
    public List<BeanEntityDef> listEntityDef() {
        return listEntityDefFromTable(null);
    }

    public List<BeanEntityDef> listEntityDefFromTable(String table) {
        List<BeanEntityDef> entityList = new ArrayList<>();
        if(dataSource instanceof DynamicDataSource && DataSourceSwitch.isEnabled()){
            String[] lookupKeys = DataSourceSwitch.getLookupKeys();
            for (String lookupKey : lookupKeys) {
                DataSourceSwitch.switchByName(lookupKey);
                entityList.addAll(listEntityDefFromTable(lookupKey,dataSource,table));
            }
            DataSourceSwitch.remove();
        }else{
            entityList = listEntityDefFromTable(null,dataSource,table);
        }

        return entityList;
    }

    public List<BeanEntityDef> listEntityDefFromTable(String lookupKey,DataSource dataSource,String table) {

        List<BeanEntityDef> entityList = new ArrayList<>();
        ResultSet tableResultSet = null;

        String dsKey = lookupKey == null ? "" : "["+ lookupKey + "]";

        try(Connection connection = dataSource.getConnection()){
            DatabaseMetaData metaData = connection.getMetaData();
            tableResultSet = metaData.getTables(null,connection.getSchema(),table,new String[]{"TABLE","VIEW"});

            //缓存Bean定义对象，用于最终整理Bean定义的包含的父Bean定义
            Map<String,BeanEntityDef> tempMap = new HashMap<>();//表名 - 表的Bean定义对象
            Map<String,List<String>> keyMapping = new HashMap<>();//表名 - 表对应的外键表的别名（外键可能多个）

            int count = 0;
            while(tableResultSet.next()){
                String tableName = tableResultSet.getString("TABLE_NAME");

                if(!analysisTable(tableName)) continue;

                String tableRemarks = tableResultSet.getString("REMARKS");
                String alias = tableAliasGenerator.genAlias(tableName);

                if(logger.isInfoEnabled()){
                    count++;

                    if(logger.isInfoEnabled()){
                        logger.info(dsKey + "分析数据表（" + count + "）：" + tableName + " - " + alias);
                    }
                }

                //获取表的外键，并加入临时缓存，用于所有表初始化后设置外键对象
                ResultSet foreignKeyResultSet = metaData.getImportedKeys(null, connection.getSchema(), tableName);
                while(foreignKeyResultSet.next()){
//                    String fkColumnName = foreignKeyResultSet.getString("FKCOLUMN_NAME");
                    String pkTablenName = foreignKeyResultSet.getString("PKTABLE_NAME");
//                    String pkColumnName = foreignKeyResultSet.getString("PKCOLUMN_NAME");
//                    String pkAlias = tableAliasGenerator.genAlias(pkTablenName);
                    List<String> fkList = keyMapping.get(tableName);
                    if(fkList == null){
                        fkList = new ArrayList<>();
                        keyMapping.put(tableName,fkList);
                    }
                    fkList.add(pkTablenName);
                }
                foreignKeyResultSet.close();

                StringBuilder pkbuilder = new StringBuilder();
                try(ResultSet primaryKeys = metaData.getPrimaryKeys(null, connection.getSchema(), tableName)){
                    while(primaryKeys.next()){
                        pkbuilder.append(primaryKeys.getString("COLUMN_NAME"));
                        pkbuilder.append(",");
                    }
                }
                String[] pkColumns = pkbuilder.toString().split(",");

                List<BeanFieldDef> fieldList = new ArrayList<>();

                try(ResultSet columns = metaData.getColumns(null, connection.getSchema(), tableName, null)){
                    while (columns.next()){
                        String columnName = columns.getString("COLUMN_NAME");
                        Integer dataType = columns.getInt("DATA_TYPE");
                        String nullable = columns.getString("IS_NULLABLE");
                        String remarks = columns.getString("REMARKS");
                        int digits = columns.getInt("DECIMAL_DIGITS");
                        int columnSize = columns.getInt("COLUMN_SIZE");
                        boolean isPk = contain(pkColumns,columnName);

                        String attrName = fieldAliasGenerator.genAlias(lookupKey,tableName,columnName);

                        //for oralce
                        if(dataType == Types.NUMERIC || dataType == Types.DECIMAL){
                            dataType = digits > 0 ? Types.NUMERIC :Types.BIGINT;
                        }

                        BeanFieldDef fieldDef = new BeanFieldDef(attrName,columnName,dataType,isPk);
                        fieldDef.setRemarks(remarks);

                        if(fieldDefCorrector != null){
                            fieldDefCorrector.correct(tableName,fieldDef);
                        }

                        fieldList.add(fieldDef);
                    }
                }

                BeanEntityDef entityDef = new BeanEntityDef(getNamespace(),alias,tableRemarks,tableName,fieldList);

                if(entityDefCorrector != null){
                    entityDefCorrector.correct(entityDef);
                }

                entityList.add(entityDef);

                tempMap.put(tableName,entityDef);
            }

            //处理所有外键对象
            //由于运行时新增表及修改表的主外键关系，均需要代码的同步调整，即肯定需要重启服务，因此不处理指定表刷新时外键更新的场景。
            Iterator<String> keys = keyMapping.keySet().iterator();
            while(keys.hasNext()){
                String pTable = keys.next();
                List<String> fkTableNameList = keyMapping.get(pTable);

                List<BeanEntityDef> parentList = new ArrayList<>();
                for (String pkTableName : fkTableNameList) {
                    BeanEntityDef parentBeanEntity = tempMap.get(pkTableName);
                    parentList.add(parentBeanEntity);
                }
                BeanEntityDef beanEntity = tempMap.get(pTable);
                beanEntity.setFkBeanEntityDef(parentList.toArray(new BeanEntityDef[0]));
            }

        } catch (SQLException e) {
            throw new RuntimeException("根据JDBC获取Bean定义错误",e);
        } finally {
            if(tableResultSet != null){
                try {
                    tableResultSet.close();
                } catch (SQLException e) {}
            }
        }

        return entityList;
    }

    @Override
    public BeanEntityDef reloadEntity(String entityCode) {
        //FIXME entityCode为编码，但下方接口参数需要表名，因为现在编码和表名一致，因此暂时可以使用。等以后用实体编码置换成表名。
        List<BeanEntityDef> beanEntityDefs = listEntityDefFromTable(entityCode);
        if(beanEntityDefs == null || beanEntityDefs.isEmpty()) return null;
        Assert.isTrue(beanEntityDefs.size() == 1,"根据实体编码查询出的实体多余1条：" + entityCode);
        return beanEntityDefs.get(0);
    }

    private boolean contain(String[] strArray,String str){
        if(strArray == null){
            return false;
        }

        for (String s : strArray) {
            if(s.equals(str)){
                return true;
            }
        }
        return false;
    }

    public void setFieldDefCorrector(FieldDefCorrector fieldDefCorrector) {
        this.fieldDefCorrector = fieldDefCorrector;
    }

    public void setEntityDefCorrector(EntityDefCorrector entityDefCorrector) {
        this.entityDefCorrector = entityDefCorrector;
    }
}
