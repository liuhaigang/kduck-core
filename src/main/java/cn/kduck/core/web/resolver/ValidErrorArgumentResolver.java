package cn.kduck.core.web.resolver;

import cn.kduck.core.web.validation.ValidError;
import cn.kduck.core.web.validation.ValidError.ValidErrorField;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ServletModelAttributeMethodProcessor;

import javax.servlet.ServletRequest;
import java.util.List;

/**
 * LiuHG
 */
public class ValidErrorArgumentResolver extends ServletModelAttributeMethodProcessor {

    public static final String VALID_ERROR = "KDUCK_VALID_ERROR";

    public ValidErrorArgumentResolver() {
        super(true);
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType() == ValidError.class;
    }

    @Override
    protected void bindRequestParameters(WebDataBinder binder, NativeWebRequest request) {
        ServletRequest servletRequest = request.getNativeRequest(ServletRequest.class);
        Assert.state(servletRequest != null, "No ServletRequest");
        ServletRequestDataBinder servletBinder = (ServletRequestDataBinder) binder;
        servletBinder.bind(servletRequest);

        ValidError validError = (ValidError)servletBinder.getTarget();
        List<ValidErrorField> errorList = (List<ValidErrorField>) servletRequest.getAttribute(VALID_ERROR);
        if(errorList != null){
            validError.setErrorList(errorList);
            servletRequest.removeAttribute(VALID_ERROR);
        }
    }
}
