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
public @interface CustomValid {

    @AliasFor("value")
    String[] name() default {};

    @AliasFor("name")
    String[] value() default {};

    Class<? extends Validator> validator();

    String message() default "{name}长度最小{min}，最大{max}";


    interface Validator{
        boolean isValid(String name, String value);
    }
}
