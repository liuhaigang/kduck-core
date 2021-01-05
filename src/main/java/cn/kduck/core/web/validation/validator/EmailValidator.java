package cn.kduck.core.web.validation.validator;

import cn.kduck.core.web.validation.ConstraintValidator;
import cn.kduck.core.web.validation.constraints.EmailValid;

import java.util.regex.Matcher;

/**
 * LiuHG
 */
public class EmailValidator implements ConstraintValidator<EmailValid> {

    private static String ATOM = "[a-z0-9!#$%&'*+/=?^_`{|}~-]";
    private static String DOMAIN = "(" + ATOM + "+(\\." + ATOM + "+)*";
    private static String IP_DOMAIN = "\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\]";

    private java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "^" + ATOM + "+(\\." + ATOM + "+)*@"
                    + DOMAIN
                    + "|"
                    + IP_DOMAIN
                    + ")$",
            java.util.regex.Pattern.CASE_INSENSITIVE
    );

    @Override
    public boolean isValid(String name, String value, EmailValid anno) {
        if(value == null) return true;
        Matcher m = pattern.matcher(value);
        return m.matches();
    }

}
