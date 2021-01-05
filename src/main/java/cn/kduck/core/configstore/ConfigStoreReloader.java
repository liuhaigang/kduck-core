package cn.kduck.core.configstore;

/**
 * 配置重载器，重新装载配置对象的属性值。装载被标记com.goldgov.kduck.configstore.annotation.ConfigStore注解的配置对象
 * @author LiuHG
 */
public interface ConfigStoreReloader<T> {

    /**
     * 重载指定配置对象的配置属性。如果无法确定configObject的类型，需要自己编码通过类反射方式重载属性值。
     * @param configCode 主配置对象编码
     * @param configObject 配置对象
     * @return 重载对象
     */
    T reloadValue(String configCode,T configObject);
}
