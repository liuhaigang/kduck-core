package cn.kduck.core.web.validation;

import cn.kduck.core.web.json.JsonObject;
import cn.kduck.core.web.validation.ValidError.ValidErrorField;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LiuHG
 */
@RestControllerAdvice
public class BindingResultAdvice {

    @ExceptionHandler(BindException.class)
    public Object validExceptionHandler(BindException e, HttpServletResponse respones){
        respones.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        JsonObject jsonObject = getValidErrorJsonObject(fieldErrors);
        return jsonObject;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object validExceptionHandler(MethodArgumentNotValidException e, HttpServletResponse respones){
        respones.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        JsonObject jsonObject = getValidErrorJsonObject(fieldErrors);
        return jsonObject;
    }

    @ExceptionHandler(ValidationException.class)
    public Object kduckValidExceptionHandler(ValidationException e, HttpServletResponse respones){
        respones.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        List<ValidErrorField> errorFields = e.getErrorFields();
        return buildErrorJsonObject(e.getMessage(),errorFields);
    }

    private JsonObject getValidErrorJsonObject(List<FieldError> fieldErrors) {
        List<ValidError> validErrorList = new ArrayList<>();
        StringBuilder infoBuilder = new StringBuilder();
        for (FieldError fieldError : fieldErrors) {
            validErrorList.add(new ValidError(fieldError.getField(), fieldError.getDefaultMessage()));
            infoBuilder.append(fieldError.getField() + fieldError.getDefaultMessage());
        }

        return buildErrorJsonObject(infoBuilder.toString(),validErrorList);
    }

    private JsonObject buildErrorJsonObject(String errorMessage,Object object){
        JsonObject jsonObject = new JsonObject(object);
        jsonObject.setMessage("数据校验失败：" + errorMessage);
        jsonObject.setCode(-2);
        return jsonObject;
    }

    public static final class ValidError{
        private final String parmName;
        private final String message;

        public ValidError(String parmName, String message) {
            this.parmName = parmName;
            this.message = message;
        }

        public String getParmName() {
            return parmName;
        }

        public String getMessage() {
            return message;
        }
    }
}
