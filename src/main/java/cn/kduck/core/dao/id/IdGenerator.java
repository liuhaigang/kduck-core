package cn.kduck.core.dao.id;

import cn.kduck.core.dao.id.impl.SnowFlakeGenerator;
import cn.kduck.core.dao.id.impl.UuidGenerator;

import java.io.Serializable;

/**
 * 主键生成策略接口，用于生成全局唯一值，一般用户数据表的主键生成，也可以根据需要应用到其他场景。
 * 该类会被框架以SpringBean的形式声明，使用注入的方式可以得到该接口实例。
 * @author LiuHG
 * @see SnowFlakeGenerator SnowFlakeGenerator
 * @see UuidGenerator UuidGenerator
 */
public interface IdGenerator {

    /**
     * 生成一个全局唯一的值
     * @return 唯一值，根据具体实现返回Serializable的具体类型值
     */
    Serializable nextId();
}
