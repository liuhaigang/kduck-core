package cn.kduck.core.web.swagger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author LiuHG
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiJsonResponse {
    String name() default "";
    String code() default "";
    Class type() default Class.class;
    String[] include() default {};

    boolean isArray() default false;

    ApiField[] value() default {};
}
