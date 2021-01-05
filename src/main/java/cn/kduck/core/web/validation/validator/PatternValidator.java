package cn.kduck.core.web.validation.validator;

import cn.kduck.core.web.validation.ConstraintValidator;
import cn.kduck.core.web.validation.constraints.PatternValid;

import java.util.regex.Matcher;


/**
 * LiuHG
 */
public class PatternValidator implements ConstraintValidator<PatternValid> {
    @Override
    public boolean isValid(String name, String value, PatternValid anno) {
        if(value == null)return true;

        int intFlag = 0;
        for ( PatternValid.Flag flag : anno.flags() ) {
            intFlag = intFlag | flag.getValue();
        }

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(anno.regexp(), intFlag);
        Matcher m = pattern.matcher( value );
        return m.matches();

    }
}
