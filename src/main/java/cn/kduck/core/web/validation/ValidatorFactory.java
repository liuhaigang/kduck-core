package cn.kduck.core.web.validation;

import cn.kduck.core.web.validation.constraints.CustomValid;
import cn.kduck.core.web.validation.constraints.EmailValid;
import cn.kduck.core.web.validation.constraints.LengthValid;
import cn.kduck.core.web.validation.constraints.NotNullValid;
import cn.kduck.core.web.validation.constraints.PatternValid;
import cn.kduck.core.web.validation.validator.CustomValidator;
import cn.kduck.core.web.validation.validator.EmailValidator;
import cn.kduck.core.web.validation.validator.PatternValidator;
import cn.kduck.core.web.validation.constraints.MaxValid;
import cn.kduck.core.web.validation.constraints.MinValid;
import cn.kduck.core.web.validation.validator.LengthValidator;
import cn.kduck.core.web.validation.validator.MaxValidator;
import cn.kduck.core.web.validation.validator.MinValidator;
import cn.kduck.core.web.validation.validator.NotNullValidator;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * LiuHG
 */
public class ValidatorFactory {

    private static final Map<Class<? extends Annotation>,ConstraintValidator> validatorMap = new HashMap<>();

    static{
        validatorMap.put(NotNullValid.class, new NotNullValidator());
        validatorMap.put(LengthValid.class, new LengthValidator());
        validatorMap.put(EmailValid.class, new EmailValidator());
        validatorMap.put(MaxValid.class, new MaxValidator());
        validatorMap.put(MinValid.class, new MinValidator());
        validatorMap.put(PatternValid.class, new PatternValidator());
        validatorMap.put(CustomValid.class, new CustomValidator());
    }

    private ValidatorFactory(){}

    public static ConstraintValidator getValidator(Class<? extends Annotation> constraintClass){
        return validatorMap.get(constraintClass);
    }

}
