package cn.kduck.core.dao;

import cn.kduck.core.dao.datasource.DataSourceSwitch;
import cn.kduck.core.dao.definition.BeanDefDepository;
import cn.kduck.core.dao.definition.BeanEntityDef;
import cn.kduck.core.dao.definition.BeanFieldDef;
import cn.kduck.core.dao.definition.FieldAliasGenerator;
import cn.kduck.core.dao.definition.TableAliasGenerator;
import cn.kduck.core.dao.sqlbuilder.SignatureInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cn.kduck.core.dao.dialect.DatabaseDialect;
import cn.kduck.core.dao.query.QuerySupport;
import cn.kduck.core.dao.query.formater.ValueFormatter;
import cn.kduck.core.dao.utils.TypeUtils;
import cn.kduck.core.utils.BeanDefUtils;
import cn.kduck.core.utils.SpringBeanUtils;
import cn.kduck.core.web.interceptor.OperateIdentificationInterceptor.OidHolder;
import cn.kduck.core.web.interceptor.OperateIdentificationInterceptor.OperateIdentification;
import cn.kduck.core.web.interceptor.operateinfo.OperateObject;
import cn.kduck.core.web.interceptor.operateinfo.OperateObject.OperateType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiElement;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.ansi.AnsiStyle;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.util.Assert;

import java.sql.*;
import java.util.*;

/**
 * @author LiuHG
 */
public class JdbcEntityDao {

    private ObjectMapper jsonMapper = new ObjectMapper();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private LobHandler lobHandler = new DefaultLobHandler();

//    @Autowired
    private BeanDefDepository beanDefDepository;

    @Autowired
    private FieldAliasGenerator attrNameGenerator;

    @Autowired
    private TableAliasGenerator tableAliasGenerator;

    @Autowired
    private List<DatabaseDialect> databaseDialectList;

    @Autowired(required = false)
    private DeleteArchiveHandler deleteArchiveHandler;

    @Value("${kduck.showSql.enabled:false}")
    private boolean showSql;

    @Value("${kduck.showSql.mode:SQL}")
    private ShowSqlMode showSqlMode;


    private void addOperateObject(OperateType type, BeanEntityDef entityDef, Map<String, Object> valueMap){
        OperateIdentification operateIdentification = OidHolder.getOperateIdentification();

        if(operateIdentification == null) {
            // 未经过审核过滤器，无法将操作对象记录进审计信息中
            return;
        }

        operateIdentification.addOperateObject(new OperateObject(type,entityDef, Collections.unmodifiableMap(valueMap)));
    }

    /**
     * 更新类SQL(新增、删除、修改)执行，新增方法应当提前设置好主键值，如果数据库为自增类型，该方法无法获取主键值，如需要得到返回的主键值，
     * 请参考{@link #execute(SqlObject, java.util.List)}方法
     * @param sqlObject SQL执行对象
     * @return 操作后返回的影响记录数
     * @see #execute(SqlObject, java.util.List)
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
//        boolean autoGeneratedKeys = true;
        processDeleteArchive(sqlObject);

        long startTime = System.currentTimeMillis();
        if (showSql && showSqlMode == ShowSqlMode.SQL) {
            printSql(-1,sqlObject.getSql(), sqlObject.getParamValueList(),null);
        }

//        LobCreator lobCreator = lobHandler.getLobCreator();
        KduckPreparedStatementCreator psc = new KduckPreparedStatementCreator(sqlObject,keyHolder != null);
//        KeyHolder keyHolder = new GeneratedKeyHolder();
        int[] returnResult = jdbcTemplate.execute(psc,ps ->{
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

        //如果输出sql模式为显示执行时间，则仅能在操作后输出sql
        if (showSql && showSqlMode == ShowSqlMode.TIME_SQL) {
            long endTime = System.currentTimeMillis();
            printSql((endTime-startTime),sqlObject.getSql(), sqlObject.getParamValueList(),null);
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
            deleteArchiveHandler.doArchive(OidHolder.getUniqueId(),entityDef,list);
            for (Map<String, Object> map : list) {
                addOperateObject(OperateType.DELETE,entityDef,map);
            }
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
            Assert.isTrue(paramValueList != null && !paramValueList.isEmpty(),"参数列表不能为空");
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
            isBatch = paramArray && paramValueList.size() > 1? true : false;
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


    //################################################## BEGIN

//    public int[] execute(SqlObject sqlObject) {
//        String sql = sqlObject.getSql();
//        List<Object> valueList = sqlObject.getParamValueList();
//
//        if(sqlObject.getSql().startsWith("DELETE") && deleteArchiveHandler != null) {
//            BeanEntityDef entityDef = sqlObject.getEntityDef();
//
//            String selectSql = sqlObject.getSql().replaceFirst("DELETE","SELECT *");
//            List<Object> paramValueList = sqlObject.getParamValueList();
//
//            List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql,paramValueList.toArray());
//            deleteArchiveHandler.doArchive(OidHolder.getUniqueId(),entityDef,list);
//            for (Map<String, Object> map : list) {
//                addOperateObject(OperateType.DELETE,entityDef,map);
//            }
//        }
//
//        long startTime = System.currentTimeMillis();
//        if (showSql && showSqlMode == ShowSqlMode.SQL) {
//            printSql(-1,sql, valueList,null);
//        }
//
//        Object value = valueList.get(0);
//
//
//        int[] returnResult;
//        if(value != null && value.getClass().isArray()){
//            if(valueList.size() == 1){
//                int result = jdbcTemplate.update(sql, (Object[]) value);
//                returnResult = new int[]{result};
//            } else {
//                List<Object[]> arrayList = new ArrayList<>(valueList.size());
//                for (Object values : valueList) {
//                    arrayList.add((Object[]) values);
//                }
//                returnResult = jdbcTemplate.batchUpdate(sql, arrayList);
//            }
//        }else{
//            returnResult = new int[]{jdbcTemplate.update(sql, valueList.toArray())};
//        }
//        if (showSql && showSqlMode == ShowSqlMode.TIME_SQL) {
//            long endTime = System.currentTimeMillis();
//            printSql((endTime-startTime),sql, valueList,null);
//        }
//        return returnResult;
//    }

    //################################################## END

    public List<Map<String,Object>> executeQuery(QuerySupport queryBean, int firstIndex, int maxRow, FieldFilter filter){
        SqlObject sqlObject = queryBean.getQuery(filter);
        Map<String, ValueFormatter> valueFormaters = queryBean.getValueFormater();

        String sql = sqlObject.getSql();

        //如果firstIndex和maxRow不在合法值范围内，则不进行分页
        if(firstIndex >= 0 && maxRow > 0){
            //拼接分页逻辑
            sql = processPage(sql,firstIndex, maxRow);
        }

        List<Object> paramList = sqlObject.getParamValueList();

        SignatureInfo signInfo = (queryBean instanceof SignatureInfo) ? (SignatureInfo)queryBean : null;
        long startTime = System.currentTimeMillis();
        if(showSql && showSqlMode == ShowSqlMode.SQL){
            printSql(-1,sql,paramList,signInfo);
        }

        List<Map<String, Object>> queryResult = jdbcTemplate.query(sql, (rs) -> {
            List<BeanFieldDef> fieldDefList = sqlObject.getFieldDefList();
            List<Map<String, Object>> recordMapList = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> recordMap = resultSet2Map(rs, fieldDefList);
                if (valueFormaters != null && !valueFormaters.isEmpty()) {
                    Iterator<String> keys = valueFormaters.keySet().iterator();
                    while (keys.hasNext()) {
                        String attrName = keys.next();
                        if (recordMap.containsKey(attrName)) {
                            ValueFormatter vf = valueFormaters.get(attrName);
                            Object v = recordMap.get(attrName);
                            recordMap.put(attrName, vf.format(v, Collections.unmodifiableMap(recordMap)));
                        }
                    }
                }
                recordMapList.add(recordMap);
            }
            return recordMapList;
        }, paramList.toArray());

        if (showSql && showSqlMode == ShowSqlMode.TIME_SQL) {
            long endTime = System.currentTimeMillis();
            printSql((endTime-startTime),sql, paramList,signInfo);
        }
        return queryResult;
    }

    //FIXME 根据数据源对象实例缓存映射数据方言对象
    private String processPage(String sql,int firstIndex, int maxRow) {
        DatabaseDialect currentDbDialect = null;
        String dbName = getDatabaseName();
        for (DatabaseDialect databaseDialect : databaseDialectList) {
            if(databaseDialect.productName().equalsIgnoreCase(dbName)){
                currentDbDialect = databaseDialect;
                break;
            }
        }

        if(currentDbDialect == null){
            throw new RuntimeException("不支持数据库的分页逻辑："+ dbName);
        }
        sql = currentDbDialect.pagingSql(sql,firstIndex,maxRow);
        return sql;
    }

    protected String getDatabaseName(){
        String dbName = null;
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()){
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
                if(fieldDef == null){
//                    throw new RuntimeException("在提供的字段定义集合中未找到指定列的字段定义：" + columnName);
                    continue;
                }
//                Object resultValue = JdbcUtils.getResultSetValue(resultSet, i + 1, fieldDef.getJavaType());

                Object resultValue;
                if(fieldDef.getJdbcType() == Types.CLOB || fieldDef.getJdbcType() == Types.NCLOB || fieldDef.getJdbcType() == Types.LONGVARCHAR || fieldDef.getJdbcType() == Types.LONGNVARCHAR){
                    //处理lob字段转换为String
                    resultValue = lobHandler.getClobAsString(resultSet, i + 1);
                }else if(fieldDef.getJdbcType() == Types.BLOB || fieldDef.getJdbcType() == Types.LONGVARBINARY || fieldDef.getJdbcType() == Types.VARBINARY || fieldDef.getJdbcType() == Types.BINARY){
                    //处理lob字段转换为byte[]
                    resultValue = lobHandler.getBlobAsBytes(resultSet, i + 1);
                }else{
                    resultValue = JdbcUtils.getResultSetValue(resultSet, i + 1, fieldDef.getJavaType());
                }

                String attrName = fieldDef.getAttrName();
                if(!columnLabel.equals(columnName)){
                    attrName = columnLabel;
                }
                recordMap.put(attrName, processIdtoString(attrName,resultValue));
            }

        }else{

            for (int i = 0; i < columnCount; i++) {
                String columnLabel = metaData.getColumnLabel(i + 1);
                String attrName = attrNameGenerator.genAlias(null,metaData.getTableName(i + 1),columnLabel);

                Object resultValue = JdbcUtils.getResultSetValue(resultSet, i + 1);

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

        SignatureInfo signInfo = (queryBean instanceof SignatureInfo) ? (SignatureInfo)queryBean : null;
        long startTime = System.currentTimeMillis();
        if(showSql && showSqlMode == ShowSqlMode.SQL){
            printSql(-1,countSql,paramList,signInfo);
        }

        Long countResult = jdbcTemplate.query(countSql, (rs) -> {
            while (rs.next()) {
                return rs.getLong(1);
            }
            return 0L;
        }, paramList.toArray());

        if (showSql && showSqlMode == ShowSqlMode.TIME_SQL) {
            long endTime = System.currentTimeMillis();
            printSql((endTime-startTime),countSql, paramList,signInfo);
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
                    BeanEntityDef entityDef = getBeanDefDepository().getEntityDef(tableCode);

                    while(rs.next()){
                        resultList.add(resultSet2Map(rs,null));
                    }
                    deleteArchiveHandler.doArchive(OidHolder.getUniqueId(),entityDef,resultList);

                    return resultList;
                },valueList.toArray());
            }
        }

        long startTime = System.currentTimeMillis();
        if(showSql && showSqlMode == ShowSqlMode.SQL){
            printSql(-1,"违规范",sql,valueList,null);
        }

        Integer executeResult = jdbcTemplate.execute(sql, (PreparedStatement statement) -> {
            for (int i = 0; i < valueList.size(); i++) {
                Object v = valueList.get(i);
                statement.setObject(i + 1, v, TypeUtils.jdbcType(v.getClass()));
            }
            return statement.executeUpdate();
        });

        if (showSql && showSqlMode == ShowSqlMode.TIME_SQL) {
            long endTime = System.currentTimeMillis();
            printSql((endTime-startTime),sql, valueList,null);
        }

        return executeResult;
    }

    protected String countSql(String sql){
        return "SELECT COUNT(*) FROM (" + sql + ") k_t";
    }

    private void printSql(long time,String sql,List<Object> params,SignatureInfo signatureInfo){
        printSql(time,null,sql, params,signatureInfo);
    }

    protected void printSql(long time,String label, String sql, List<Object> paramList, SignatureInfo signatureInfo){
        List printParam = new ArrayList(paramList.size());
        String paramJson;
        try {
            for (int i = 0; i < paramList.size(); i++) {
                Object rowParam = paramList.get(i);
                if(rowParam.getClass().isArray()){
                    Object[] paramItems = (Object[])rowParam;
                    Object[] tempItems = new Object[paramItems.length];
                    for (int i1 = 0; i1 < paramItems.length; i1++) {
                        Object paramItem = paramItems[i1];
                        tempItems[i1] = unwrapParamValue(paramItem);
                    }
                    printParam.add(tempItems);
                }else{
                    printParam.add(unwrapParamValue(rowParam));
                }
            }
            paramJson =  jsonMapper.writeValueAsString(printParam);
        } catch (JsonProcessingException e) {
            paramJson = "【参数值转换JSON错误】";
        }

        String spendTime = "";
        if(time >= 0 ){
            AnsiElement color = time >= 500 ? AnsiColor.RED:AnsiColor.YELLOW;
            spendTime = AnsiOutput.toString(color,"(" + time + "ms)");
        }

        if(label == null){
            label = "";
        }else{
            label =  AnsiOutput.toString(AnsiColor.RED,"【" + label + "】");
        }

        String generateBy = "";
        if(signatureInfo != null && signatureInfo.generateBy() != null){
            generateBy = AnsiOutput.toString("; ",AnsiColor.YELLOW,"QUERY:",AnsiColor.DEFAULT,signatureInfo.generateBy());
        }

        String dsKey = "";
        if(DataSourceSwitch.isEnabled()){
            dsKey = "[" + DataSourceSwitch.get() + "]";
        }

        String printSql = AnsiOutput.toString(
                AnsiStyle.BOLD,
                label,
                spendTime,
                AnsiStyle.BOLD,
                AnsiColor.YELLOW,
                dsKey,
                "SQL:",
                AnsiColor.BLUE,
                sql+"; ",
                AnsiColor.YELLOW,
                "PARAMS:",
                AnsiColor.DEFAULT,
                paramJson,
                generateBy,
                AnsiStyle.NORMAL);
        System.out.println(printSql);
    }

    private Object unwrapParamValue(Object paramItem) {
        if(paramItem instanceof SqlParameterValue){
            SqlParameterValue pv = (SqlParameterValue)paramItem;
            Object value = pv.getValue();
            if(value != null && (pv.getSqlType() == Types.LONGVARCHAR || pv.getSqlType() == Types.LONGNVARCHAR || pv.getSqlType() == Types.LONGVARBINARY)){
                return "<LOB>";
            }else{
                return value;
            }
        }else{
            return paramItem;
        }
    }

    private enum ShowSqlMode {
        SQL,TIME_SQL;
    }


    private BeanDefDepository getBeanDefDepository(){
        if(beanDefDepository == null){
            beanDefDepository = SpringBeanUtils.getBean(BeanDefDepository.class);
        }
        return beanDefDepository;
    }

//    private class KduckPreparedStatementSetter extends ArgumentPreparedStatementSetter {
//
//        public KduckPreparedStatementSetter(Object[] args) {
//            super(args);
//        }
//
//        @Override
//        protected void doSetValue(PreparedStatement ps, int parameterPosition, Object argValue) throws SQLException {
//            if(argValue != null && argValue.getClass() == byte[].class){
//                defaultLobHandler.getLobCreator().setBlobAsBytes(ps,parameterPosition,(byte[])argValue);
//            }else{
//                super.doSetValue(ps, parameterPosition, argValue);
//            }
//        }
//    }
}
