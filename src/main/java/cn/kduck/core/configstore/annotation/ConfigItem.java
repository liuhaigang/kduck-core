package cn.kduck.core.configstore.annotation;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigItem {

    String name()default "";//当配置对象包含配置对象时，避免属性重名时定义。
    String explain();
    String defaultValue() default "";
    String hint() default "";
    String group() default "";
    int order() default 0;
}
