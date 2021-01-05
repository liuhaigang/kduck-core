package cn.kduck.core.web.validation.constraints;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * LiuHG
 */
@Target({ METHOD})
@Retention(RUNTIME)
@Documented
public @interface MaxValid {

    @AliasFor("value")
    String[] name() default {};

    @AliasFor("name")
    String[] value() default {};

    long max();

    String message() default "{name}最大不能超过{max}";

}
