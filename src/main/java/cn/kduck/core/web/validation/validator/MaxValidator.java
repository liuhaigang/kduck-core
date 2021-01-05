package cn.kduck.core.web.validation.validator;

import cn.kduck.core.web.validation.ConstraintValidator;
import cn.kduck.core.web.validation.constraints.MaxValid;

/**
 * LiuHG
 */
public class MaxValidator implements ConstraintValidator<MaxValid> {

    @Override
    public boolean isValid(String name, String value, MaxValid anno) {

        if(value == null){
            return true;
        }

        long max = anno.max();

        if(Long.valueOf(value) <= max){
            return true;
        }

        return false;
    }

}
