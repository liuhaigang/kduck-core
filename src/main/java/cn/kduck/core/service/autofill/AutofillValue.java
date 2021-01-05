package cn.kduck.core.service.autofill;

import cn.kduck.core.service.ValueBean;

/**
 * 自动填充接口，用于全局设置属性值。
 * @author LiuHG
 */
public interface AutofillValue {


    /**
     *
     * @param type 当前操作的类型，支持新增{@link FillType#ADD}和修改{@link FillType#UPDATE}操作
     * @param valueMap 参数值
     */
    void autofill(FillType type, ValueBean valueMap);

    enum FillType {
        ADD,UPDATE;
    }
}
