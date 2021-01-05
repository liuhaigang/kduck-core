package cn.kduck.core.web.validation.validator;

import cn.kduck.core.web.validation.ConstraintValidator;
import cn.kduck.core.web.validation.constraints.NotNullValid;

/**
 * LiuHG
 */
public class NotNullValidator implements ConstraintValidator<NotNullValid> {

    @Override
    public boolean isValid(String name, String value, NotNullValid anno) {
        return value != null;
    }

}
