package cn.kduck.core.web.validation;


import java.lang.annotation.Annotation;

/**
 * LiuHG
 */
public interface ConstraintValidator<A extends Annotation> {

    boolean isValid(String name, String value, A anno);

}
