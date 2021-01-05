package cn.kduck.core.web.validation.validator;

import cn.kduck.core.web.validation.ConstraintValidator;
import cn.kduck.core.web.validation.constraints.MinValid;

/**
 * LiuHG
 */
public class MinValidator implements ConstraintValidator<MinValid> {

    @Override
    public boolean isValid(String name, String value, MinValid anno) {

        if(value == null){
            return true;
        }

        long min = anno.min();

        if(Long.valueOf(value) >= min){
            return true;
        }

        return false;
    }

}
