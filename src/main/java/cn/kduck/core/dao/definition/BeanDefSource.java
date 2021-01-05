package cn.kduck.core.dao.definition;

import java.util.List;

/**
 * LiuHG
 */
public interface BeanDefSource {

    String getNamespace();

    /**
     * 实体定义加载方法，用于加载具体实体及字段定义信息，最终会将返回的定义对象放入到BeanDefDepository中用于系统使用。<p>由于该方法是在系统启动过程中调用，
     * 并负责实体定义的初始化，因此建议不要在该方法中使用DefaultService或继承该类的类实例，因为在DefaultService中间接使用到了本接口实例
     * ，很容易造成循环依赖的错误或由于循环依赖错误导致的NullPointerException，如需要使用数据连接，建议使用JdbcEntityDao或JdbcTemplate。
     * @return 所有实体定义对象
     */
    List<BeanEntityDef> listEntityDef();

    /**
     * 重载指定编码的实体信息
     * @param entityCode
     * @return 返回重载后的实体信息
     */
    BeanEntityDef reloadEntity(String entityCode);
}
