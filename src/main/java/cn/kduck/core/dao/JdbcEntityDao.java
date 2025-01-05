package cn.kduck.core.dao;

import cn.kduck.core.KduckProperties.ShowSqlMode;
import cn.kduck.core.dao.definition.*;
import cn.kduck.core.dao.dialect.DatabaseDialect;
import cn.kduck.core.dao.query.QuerySupport;
import cn.kduck.core.dao.query.formater.ValueFormatter;
import cn.kduck.core.dao.sqllog.ShowSqlLogger;
import cn.kduck.core.dao.sqllog.impl.EmptyShowSqlLogger;
import cn.kduck.core.dao.utils.TypeUtils;
import cn.kduck.core.utils.BeanDefUtils;
import cn.kduck.core.web.interceptor.OperateIdentificationInterceptor.OidHolder;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.*;

/**
 * @author LiuHG
 */
public class JdbcEntityDao {

    private JdbcTemplate jdbcTemplate;
    private List<DatabaseDialect> databaseDialectList;

    private LobHandler lobHandler = new DefaultLobHandler();

    private FieldAliasGenerator attrNameGenerator = new DefaultFieldAliasGenerator();
    private TableAliasGenerator tableAliasGenerator = new DefaultTableAliasGenerator();

    private DeleteArchiveHandler deleteArchiveHandler;

    private ShowSqlLogger sqlLogger = new EmptyShowSqlLogger();
    private ShowSqlMode showSqlMode = ShowSqlMode.SQL;

    private Map<DataSource,DatabaseDialect> dialectCache = new HashMap<>(3);

    public JdbcEntityDao(DataSource dataSource, List<DatabaseDialect> databaseDialectList){
        this.databaseDialectList = databaseDialectList;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

//    private void addOperateObject(OperateType type, BeanEntityDef entityDef, Map<String, Object> valueMap){
//        OperateIdentification operateIdentification = OidHolder.getOperateIdentification();
//
//        if(operateIdentification == null) {
//            // 未经过审核过滤器，无法将操作对象记录进审计信息中
//            return;
//        }
//
//        operateIdentification.addOperateObject(new OperateObject(type,entityDef, Collections.unmodifiableMap(valueMap)));
//    }

    /**
     * 更新类SQL(新增、删除、修改)执行，新增方法应当提前设置好主键值，如果数据库为自增类型，该方法无法获取主键值，如需要得到返回的主键值，
     * 请参考{@link #execute(SqlObject, List)}方法
     * @param sqlObject SQL执行对象
     * @return 操作后返回的影响记录数
     * @see #execute(SqlObject, List)
     */
    public int[] execute(SqlObject sqlObject) {
        return execute(sqlObject,null);
    }

    /**
     * 更新类SQL(新增、删除、修改)执行，根据SQL参数来自动判断按照单条或批量执行，可根据keyHolder参数获取自增主键值
     * @param sqlObject SQL执行对象
     * @param keyHolder 主键钩子对象，当操作为insert且数据表主键为数据库自增类型，则会将入库后的主键通过该参数返回，其他操作该参数长度始终为0
     *                  （提示：数据库每张表只会有1个自增主键列，如果手动设置了主键值，则不会使用自增值），
     *                  可为null，值为null时则不获取返回的主键，等同于{@link #execute(SqlObject)}方法
     * @return 操作后返回的影响记录数
     * @see #execute(SqlObject)
     */
    public int[] execute(SqlObject sqlObject,List<Object> keyHolder) {
        processDeleteArchive(sqlObject);

        long startTime = System.currentTimeMillis();
        if (showSqlMode == ShowSqlMode.SQL) {
            sqlLogger.sqlLog(sqlObject.getSql(), sqlObject.getParamValueList());
        }

        int[] returnResult;
        try{
            KduckPreparedStatementCreator psc = new KduckPreparedStatementCreator(sqlObject,keyHolder != null);
            returnResult = jdbcTemplate.execute(psc,ps ->{
                int[] rows;
                if(psc.isBatch()){
                    rows = ps.executeBatch();
                }else{
                    rows = new int[]{ps.executeUpdate()};
                }

                if(keyHolder != null){
//                List<Map<String, Object>> generatedKeys = keyHolder.getKeyList();
//                generatedKeys.clear();
                    keyHolder.clear();
                    ResultSet keys = ps.getGeneratedKeys();
                    if (keys != null) {
                        try {
                            RowMapperResultSetExtractor<Map<String, Object>> rse =
                                    new RowMapperResultSetExtractor<>(new ColumnMapRowMapper(), 1);
//                        generatedKeys.addAll(rse.extractData(keys));
                            List<Map<String, Object>> keyMapList = rse.extractData(keys);
                            keyMapList.stream().forEach(keyMap ->keyHolder.addAll(keyMap.values()));
                        }
                        finally {
                            JdbcUtils.closeResultSet(keys);
                        }
                    }
                }
                return rows;
            });
        }catch (Exception e){
            if (showSqlMode == ShowSqlMode.SQL_ON_ERROR) {
                sqlLogger.errorSqlLog(sqlObject.getSql(), sqlObject.getParamValueList(),e);
            }
            throw e;
        }


        //如果输出sql模式为显示执行时间，则仅能在操作后输出sql
        if (showSqlMode == ShowSqlMode.TIME_SQL || showSqlMode == ShowSqlMode.JUST_SLOW_SQL) {
            long endTime = System.currentTimeMillis();
            sqlLogger.timeSqlLog((endTime-startTime),sqlObject.getSql(), sqlObject.getParamValueList());
        }
        return returnResult;
    }

    /**
     * 当操作为删除操作时，根据deleteArchiveHandler的实现，处理被删除数据的归档逻辑
     * @param sqlObject
     */
    private void processDeleteArchive(SqlObject sqlObject) {
        if(sqlObject.getSql().startsWith("DELETE") && deleteArchiveHandler != null) {
            BeanEntityDef entityDef = sqlObject.getEntityDef();

            StringJoiner stringJoiner = new StringJoiner(",");
            List<BeanFieldDef> fieldList = entityDef.getFieldList();
            for (BeanFieldDef beanFieldDef : fieldList) {
                int jdbcType = beanFieldDef.getJdbcType();
                if(jdbcType == Types.LONGVARCHAR || jdbcType == Types.LONGNVARCHAR || jdbcType == Types.LONGVARBINARY){
                    continue;
                }
                stringJoiner.add(beanFieldDef.getFieldName());
            }

            String selectSql = sqlObject.getSql().replaceFirst("DELETE","SELECT " + stringJoiner);
            List<Object> paramValueList = sqlObject.getParamValueList();

            List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql,paramValueList.toArray());
            deleteArchiveHandler.doArchive(OidHolder.getUniqueId(),entityDef.getEntityCode(),list);
//            for (Map<String, Object> map : list) {
//                addOperateObject(OperateType.DELETE,entityDef,map);
//            }
        }
    }

    public static class KduckPreparedStatementCreator implements PreparedStatementCreator{

        private final String sql;
        private final List<Object> paramValueList;
        private final boolean returnKey;
        private final boolean isBatch;
        private boolean paramArray;

        public KduckPreparedStatementCreator(SqlObject sqlObject,boolean returnKey ){
            this.sql = sqlObject.getSql();
            this.paramValueList = sqlObject.getParamValueList();
            this.returnKey = returnKey;
//            Assert.isTrue(paramValueList != null && !paramValueList.isEmpty(),"参数列表不能为空");
            if(paramValueList.size() > 0){
                Object value = paramValueList.get(0);
                paramArray = value != null && value.getClass().isArray();
                if(paramArray){
                    for (Object o : paramValueList) {
                        if(o.getClass() != value.getClass()){
                            paramArray = false;
                            break;
                        }
                    }
                }
                isBatch = paramArray && paramValueList.size() > 1;
            }else{
                isBatch = false;
            }
        }

        @Override
        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            PreparedStatement ps;
            if(returnKey){
                ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            }else{
                ps = con.prepareStatement(sql);
            }

            if(isBatch){
                int batchSize = paramValueList.size();
                BatchArgumentPreparedStatementSetter pss = new BatchArgumentPreparedStatementSetter(paramValueList);
                for (int i = 0; i < batchSize; i++) {
                    pss.setValues(ps,i);
                    ps.addBatch();
                }
            }else{
                Object[] paramObjs;
                if(paramArray){
                    paramObjs = (Object[]) paramValueList.get(0);
                }else{
                    paramObjs = paramValueList.toArray();
                }
                ArgumentPreparedStatementSetter pss = new ArgumentPreparedStatementSetter(paramObjs);
                pss.setValues(ps);
            }
            return ps;
        }

        public boolean isBatch() {
            return isBatch;
        }
    }

    public static class BatchArgumentPreparedStatementSetter implements BatchPreparedStatementSetter{

        private final List<Object> paramValueList;

        public BatchArgumentPreparedStatementSetter(List<Object> paramValueList){
            this.paramValueList = paramValueList;
        }

        @Override
        public void setValues(PreparedStatement ps, int i) throws SQLException {
            Object[] values = (Object[]) paramValueList.get(i);
            int colIndex = 0;
            for (Object value : values) {
                colIndex++;
                if (value instanceof SqlParameterValue) {
                    SqlParameterValue paramValue = (SqlParameterValue) value;
                    StatementCreatorUtils.setParameterValue(ps, colIndex, paramValue, paramValue.getValue());
                }
                else {
                    StatementCreatorUtils.setParameterValue(ps, colIndex, SqlTypeValue.TYPE_UNKNOWN, value);
                }
            }
        }

        @Override
        public int getBatchSize() {
            return paramValueList.size();
        }
    }

    public List<Map<String,Object>> executeQuery(QuerySupport queryBean, int firstIndex, int maxRow, FieldFilter filter){
        return executeQuery(queryBean,firstIndex,maxRow,filter,null);
    }

    public void executeQuery(QuerySupport queryBean, FieldFilter filter, BatchDataCallbackHandler batchDataCallbackHandler){
        executeQuery(queryBean,-1,-1,filter, batchDataCallbackHandler);
    }

    private List<Map<String,Object>> executeQuery(QuerySupport queryBean, int firstIndex, int maxRow, FieldFilter filter,
                                                 BatchDataCallbackHandler batchDataCallbackHandler){
        SqlObject sqlObject = queryBean.getQuery(filter);
        Map<String, ValueFormatter> valueFormaters = queryBean.getValueFormater();

        String sql = sqlObject.getSql();

        //如果firstIndex和maxRow不在合法值范围内，则不进行分页
        if(firstIndex >= 0 && maxRow > 0){
            //拼接分页逻辑
            sql = processPage(sql,firstIndex, maxRow);
        }

        List<Object> paramList = sqlObject.getParamValueList();

        long startTime = System.currentTimeMillis();
        if(showSqlMode == ShowSqlMode.SQL){
            sqlLogger.sqlLog(sql,paramList,queryBean.generateBy());
        }

        List<Map<String, Object>> queryResult;
        try{
            queryResult = jdbcTemplate.query(sql, (rs) -> {
                List<BeanFieldDef> fieldDefList = sqlObject.getFieldDefList();
                List<Map<String, Object>> recordMapList = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> recordMap = resultSet2Map(rs, fieldDefList);
                    if (valueFormaters != null && !valueFormaters.isEmpty()) {
                        for (String attrName : valueFormaters.keySet()) {
                            if (recordMap.containsKey(attrName)) {
                                ValueFormatter vf = valueFormaters.get(attrName);
                                Object v = recordMap.get(attrName);
                                recordMap.put(attrName, vf.format(v, Collections.unmodifiableMap(recordMap)));
                            }
                        }
                    }
                    recordMapList.add(recordMap);
                    if(batchDataCallbackHandler != null && recordMapList.size() == batchDataCallbackHandler.batchSize()){
                        Map[] recordMapArray = recordMapList.stream().toArray(LinkedHashMap[]::new);
                        batchDataCallbackHandler.processBatchData(recordMapArray);
                        recordMapList.clear();
                    }
                }

                if(batchDataCallbackHandler != null && !recordMapList.isEmpty()){
                    Map[] recordMapArray = recordMapList.stream().toArray(LinkedHashMap[]::new);
                    batchDataCallbackHandler.processBatchData(recordMapArray);
                    recordMapList.clear();
                }
                return recordMapList;
            }, paramList.toArray());
        }catch (Exception e){
            if (showSqlMode == ShowSqlMode.SQL_ON_ERROR) {
                sqlLogger.errorSqlLog(sql, paramList,e,queryBean.generateBy());
            }
            throw e;
        }


        if (showSqlMode == ShowSqlMode.TIME_SQL || showSqlMode == ShowSqlMode.JUST_SLOW_SQL) {
            long endTime = System.currentTimeMillis();
            sqlLogger.timeSqlLog((endTime-startTime),sql, paramList,queryBean.generateBy());
        }
        return queryResult;
    }

    //FIXME 根据数据源对象实例缓存映射数据方言对象
    private String processPage(String sql,int firstIndex, int maxRow) {
        DataSource dataSource = jdbcTemplate.getDataSource();
        DatabaseDialect currentDbDialect = dialectCache.get(dataSource);
        if(currentDbDialect == null){
            String dbName = getDatabaseName(dataSource);
            for (DatabaseDialect databaseDialect : databaseDialectList) {
                if(databaseDialect.productName().equalsIgnoreCase(dbName)){
                    currentDbDialect = databaseDialect;
                    dialectCache.put(dataSource,currentDbDialect);
                    break;
                }
            }

            if(currentDbDialect == null){
                throw new RuntimeException("不支持数据库的分页逻辑："+ dbName);
            }
        }

        sql = currentDbDialect.pagingSql(sql,firstIndex,maxRow);
        return sql;
    }

    protected String getDatabaseName(DataSource dataSource){
        String dbName = null;
        try (Connection connection = dataSource.getConnection()){
            dbName = connection.getMetaData().getDatabaseProductName();
        } catch (SQLException e) {
            throw new RuntimeException("获取数据库类型错误："+ dbName,e);
        }
        if(dbName == null){
            dbName = "unknow";
        }
        return dbName;
    }

    /**
     * 将查询结果集封装成Map类型
     * @param resultSet 结果集对象
     * @param fieldDefList 字段定义集合，根据该参数返回字段值，不包含在改字段集合中的字段，不会被提取返回
     * @return 封装成Map对象的结果集合
     * @throws SQLException 操作结果集对象时可能的SQL异常
     */
    private Map<String,Object> resultSet2Map(ResultSet resultSet, List<BeanFieldDef> fieldDefList) throws SQLException {
        Map<String,Object> recordMap = new LinkedHashMap<>();

        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        if(fieldDefList != null && !fieldDefList.isEmpty()){
            for (int i = 0; i < columnCount; i++) {
                String columnName = metaData.getColumnName(i + 1);
                String columnLabel = metaData.getColumnLabel(i + 1);
                BeanFieldDef fieldDef = BeanDefUtils.getByColName(fieldDefList, columnName);

                String attrName;
                Object resultValue = null;
                if(fieldDef != null){
                    if(fieldDef.getJdbcType() == Types.CLOB || fieldDef.getJdbcType() == Types.NCLOB || fieldDef.getJdbcType() == Types.LONGVARCHAR || fieldDef.getJdbcType() == Types.LONGNVARCHAR){
                        //处理lob字段转换为String
                        resultValue = lobHandler.getClobAsString(resultSet, i + 1);
                    }else if(fieldDef.getJdbcType() == Types.BLOB || fieldDef.getJdbcType() == Types.LONGVARBINARY || fieldDef.getJdbcType() == Types.VARBINARY || fieldDef.getJdbcType() == Types.BINARY){
                        //处理lob字段转换为byte[]
                        resultValue = lobHandler.getBlobAsBytes(resultSet, i + 1);
                    } else if(fieldDef.getJdbcType() == Types.TIMESTAMP){
                        Timestamp timestamp = resultSet.getTimestamp(i + 1);
                        if(timestamp != null){
                            resultValue = new Date(timestamp.getTime());
                        }
                    } else if(fieldDef.getJdbcType() == Types.DATE){
                        Date date = resultSet.getDate(i + 1);
                        if(date != null){
                            resultValue = new Date(date.getTime());
                        }
                    }else{
                        resultValue = JdbcUtils.getResultSetValue(resultSet, i + 1, fieldDef.getJavaType());
                    }
                    attrName = fieldDef.getAttrName();
                    if(!columnLabel.equals(columnName)){
                        attrName = columnLabel;
                    }
                } else {
                    attrName = columnLabel;
                    resultValue = JdbcUtils.getResultSetValue(resultSet, i + 1);
                }
                recordMap.put(attrName, processIdtoString(attrName,resultValue));
            }

        }else{

            for (int i = 0; i < columnCount; i++) {
                String columnName = metaData.getColumnName(i + 1);
                String columnLabel = metaData.getColumnLabel(i + 1);

                String attrName;
                if(columnLabel.equals(columnName)){
                    attrName = attrNameGenerator.genAlias(null,metaData.getTableName(i + 1),columnLabel);
                }else{
                    attrName = columnLabel;
                }

                Object resultValue = JdbcUtils.getResultSetValue(resultSet, i + 1);

                //如果返回为LocalDateTime对象，则转换为Date对象放入结果集中。
                if(resultValue instanceof LocalDateTime){
                    ZonedDateTime zonedDateTime = ((LocalDateTime) resultValue).atZone(ZoneId.systemDefault());
                    resultValue = Date.from(zonedDateTime.toInstant());
                }

                recordMap.put(attrName, processIdtoString(attrName,resultValue));

            }
        }

        return recordMap;
    }

    /*
     * 处理js不支持长long，将long转换为字符串供页面输出
     */
    private Object processIdtoString(String attrName,Object attrValue){
        if(attrName.endsWith("Id") && attrValue != null && (attrValue.getClass() == Long.TYPE || attrValue.getClass() == Long.class)){
            return attrValue.toString();
        }else{
            return attrValue;
        }

    }

    public long executeCount(QuerySupport queryBean,FieldFilter filter){
        SqlObject sqlObject = queryBean.getQuery(filter);

        String sql = sqlObject.getSql();
        String countSql = countSql(sql);

        List<Object> paramList = sqlObject.getParamValueList();

        long startTime = System.currentTimeMillis();
        if(showSqlMode == ShowSqlMode.SQL){
            sqlLogger.sqlLog(countSql,paramList,queryBean.generateBy());
        }

        Long countResult;
        try{
            countResult = jdbcTemplate.query(countSql, (rs) -> {
                long count = 0L;
                while (rs.next()) {
                    count = rs.getLong(1);
                }
                return count;
            }, paramList.toArray());
        }catch (Exception e){
            if (showSqlMode == ShowSqlMode.SQL_ON_ERROR) {
                sqlLogger.errorSqlLog(sql, paramList,e,queryBean.generateBy());
            }
            throw e;
        }


        if (showSqlMode == ShowSqlMode.TIME_SQL || showSqlMode == ShowSqlMode.JUST_SLOW_SQL) {
            long endTime = System.currentTimeMillis();
            sqlLogger.timeSqlLog((endTime-startTime),countSql, paramList,queryBean.generateBy());
        }
        return countResult;
    }

    public long executeCount(QuerySupport queryBean){
        return executeCount(queryBean,null);

    }

    /**
     * 写操作SQL执行接口，提供最原始的SQL语句及相关参数。该方法需要经过批准后才可以调用。
     * @param sql 包含占位符的SQL写操作语句
     * @param paramMap 相关参数Map
     * @return 影响的数据条数
     */
    public int execute(String sql, Map<String,Object> paramMap){

        List<Object> valueList = cn.kduck.core.dao.utils.JdbcUtils.getValueList(sql,paramMap);
        sql = sql.replaceAll(cn.kduck.core.dao.utils.JdbcUtils.PLACEHOLDER_PATTERN,"?");

        if(deleteArchiveHandler != null ){
            if(sql.trim().startsWith("DELETE")){
                String selectSql = sql.replaceFirst("DELETE","SELECT *");
                List<Map<String,Object>> resultList = new ArrayList<>();
                jdbcTemplate.query(selectSql,(ResultSet rs)->{
                    String tableName = rs.getMetaData().getTableName(1);
                    String tableCode = tableAliasGenerator.genAlias(tableName);

                    while(rs.next()){
                        resultList.add(resultSet2Map(rs,null));
                    }
                    deleteArchiveHandler.doArchive(OidHolder.getUniqueId(),tableCode,resultList);

                    return resultList;
                },valueList.toArray());
            }
        }

        long startTime = System.currentTimeMillis();
        if(showSqlMode == ShowSqlMode.SQL){
            sqlLogger.sqlLog(sql,valueList,null,true);
        }

        Integer executeResult;
        try{
            executeResult = jdbcTemplate.execute(sql, (PreparedStatement statement) -> {
                for (int i = 0; i < valueList.size(); i++) {
                    Object v = valueList.get(i);
                    statement.setObject(i + 1, v, TypeUtils.jdbcType(v.getClass()));
                }
                return statement.executeUpdate();
            });
        }catch (Exception e){
            if (showSqlMode == ShowSqlMode.SQL_ON_ERROR) {
                sqlLogger.errorSqlLog(sql, valueList,e);
            }
            throw e;
        }

        if (showSqlMode == ShowSqlMode.TIME_SQL || showSqlMode == ShowSqlMode.JUST_SLOW_SQL) {
            long endTime = System.currentTimeMillis();
            sqlLogger.timeSqlLog((endTime-startTime),sql, valueList,null,true);
        }

        return executeResult;
    }

    protected String countSql(String sql){
        return "SELECT COUNT(*) FROM (" + sql + ") k_t";
    }

    public void setSqlLogger(ShowSqlLogger sqlLogger) {
        this.sqlLogger = sqlLogger;
    }

    public void setShowSqlMode(ShowSqlMode showSqlMode) {
        this.showSqlMode = showSqlMode;
    }

    public void setDeleteArchiveHandler(DeleteArchiveHandler deleteArchiveHandler) {
        this.deleteArchiveHandler = deleteArchiveHandler;
    }
}
