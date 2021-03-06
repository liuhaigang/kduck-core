package cn.kduck.core.web.interceptor;

import cn.kduck.core.dao.definition.BeanEntityDef;
import cn.kduck.core.dao.definition.BeanFieldDef;
import cn.kduck.core.service.ValueMap;
import cn.kduck.core.web.interceptor.operateinfo.OperateObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * LiuHG
 */
public class OperateIdentificationInterceptor implements HandlerInterceptor {

    private static ThreadLocal<OperateIdentification> optObject = new ThreadLocal<>();
//    private static ThreadLocal<StopWatch> stopWatchThreadLocal = new ThreadLocal<>();

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String oid = UUID.randomUUID().toString().replaceAll("-", "");
        optObject.set(new OperateIdentification(oid));
//        stopWatchThreadLocal.set(new StopWatch());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        OperateIdentification operateIdentification = optObject.get();
        if(operateIdentification != null){
            operateIdentification.clear();
            optObject.remove();
        }
//        stopWatchThreadLocal.remove();
    }

    public static class OperateIdentification {
        private final String uniqueId;
        private final List<OperateObject> operateObjectList = new ArrayList<>();

        public OperateIdentification(String uniqueId){
            this.uniqueId = uniqueId;
        }

        public void addOperateObject(OperateObject operateObject){

//            cleanOperateObject(operateObject);
            operateObjectList.add(operateObject);
        }

        private void cleanOperateObject(OperateObject operateObject) {
            ValueMap valueMap = operateObject.getValueMap();
            BeanEntityDef entityDef = operateObject.getEntityDef();
            List<BeanFieldDef> fieldList = entityDef.getFieldList();
            for (BeanFieldDef beanFieldDef : fieldList) {
                if(beanFieldDef.getJdbcType() == Types.BLOB) {
                    valueMap.remove(beanFieldDef.getAttrName());
                }
            }
        }

        public String getUniqueId() {
            return uniqueId;
        }

        public void clear(){
            operateObjectList.clear();
        }

        public List<OperateObject> getOperateObjectList() {
            return Collections.unmodifiableList(operateObjectList);
        }

    }

    public static class OidHolder {

        private static final Log logger = LogFactory.getLog(OidHolder.class);

        public static String getUniqueId(){
            OperateIdentification oid = optObject.get();
            if(oid == null){
                logger.warn("?????????Controller?????????????????????????????????ID?????????????????????????????????????????????");
                return "[GROUP FAIL]";
            }
            return oid.getUniqueId();
        }

        public static OperateIdentification getOperateIdentification(){
            return optObject.get();
        }
    }

//    public static class StopWatchHolder {
//
//        private static final Log logger = LogFactory.getLog(StopWatchHolder.class);
//
//        public static StopWatch getStopWatch(){
//            StopWatch stopWatch = stopWatchThreadLocal.get();
//            if(stopWatch == null){
//                stopWatch = new StopWatch();
//                stopWatchThreadLocal.set(stopWatch);
//            }
//            return stopWatch;
//        }
//
//        public static void start(String taskName){
//            getStopWatch().start(taskName);
//        }
//
//        public static void stopAndStart(String taskName){
//            StopWatch stopWatch = getStopWatch();
//            stopWatch.stop();
//            stopWatch.start(taskName);
//        }
//
//        public static void stop(){
//            getStopWatch().stop();
//        }
//
//        public static String getSummaryInfo(){
//            return getStopWatch().prettyPrint();
//        }
//    }
}
