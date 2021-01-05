package cn.kduck.core.web.validation.validator;

import cn.kduck.core.web.validation.ConstraintValidator;
import cn.kduck.core.web.validation.constraints.LengthValid;

/**
 * LiuHG
 */
public class LengthValidator implements ConstraintValidator<LengthValid> {

    @Override
    public boolean isValid(String name, String value, LengthValid anno) {

        if(value == null){
            return true;
        }
        long min = anno.min();
        long max = anno.max();

        if ( max < min ) {
            throw new IllegalArgumentException( "The length cannot be negative." );
        }

        if(value.length() >= min && value.length() <= max){
            return true;
        }

        return false;
    }

}
