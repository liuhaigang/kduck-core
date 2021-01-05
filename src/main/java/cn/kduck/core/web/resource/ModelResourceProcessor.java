package cn.kduck.core.web.resource;

/**
 * LiuHG
 */
public interface ModelResourceProcessor {

//    /**
//     * @param resource
//     * @deprecated 由 {@link #doProcess(ResourceValueMap, Class)} 方法代替
//     */
//    default void doProcess(ResourceValueMap resource){}

    void doProcess(ResourceValueMap resource,Class clazz);
}
