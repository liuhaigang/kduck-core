package cn.kduck.core.web.swagger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author LiuHG
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiField {

    String value() default "";
    String name() default "";
    String allowableValues() default "";
    String notes() default "";
    String dataType() default "";
    boolean required() default false;
    String example() default "";
    String reference() default "";
    int position() default 0;


    boolean allowMultiple() default false;
    String defaultValue() default "";
    String paramType() default "";

}
