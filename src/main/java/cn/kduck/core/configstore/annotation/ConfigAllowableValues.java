package cn.kduck.core.configstore.annotation;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigAllowableValues {
    String[] value();
}
