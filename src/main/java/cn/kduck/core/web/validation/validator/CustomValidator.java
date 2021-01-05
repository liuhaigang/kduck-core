package cn.kduck.core.web.validation.validator;

import org.springframework.beans.BeanUtils;
import cn.kduck.core.web.validation.ConstraintValidator;
import cn.kduck.core.web.validation.constraints.CustomValid;
import cn.kduck.core.web.validation.constraints.CustomValid.Validator;

import java.util.HashMap;
import java.util.Map;


/**
 * LiuHG
 */
public class CustomValidator implements ConstraintValidator<CustomValid> {

    private static final Map<Class<? extends Validator>,Validator> validatorMap = new HashMap<Class<? extends Validator>,Validator>();

    @Override
    public boolean isValid(String name, String value, CustomValid anno) {
        if(value == null)return true;
        Class<? extends Validator> validator = anno.validator();

        Validator v;
        if(validatorMap.containsKey(validator)){
            v = validatorMap.get(validator);
        }else{
            try {
                v = BeanUtils.instantiateClass(validator);
                validatorMap.put(validator, v);
            } catch (Exception e) {
                throw new RuntimeException("无法根据给定的验证Class类构造实例，请确保类存在且包含一个无参的构造器：" + validator,e);
            }
        }
        return v.isValid(name,value);

    }
}
