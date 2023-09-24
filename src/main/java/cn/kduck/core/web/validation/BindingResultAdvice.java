package cn.kduck.core.web.validation;

import cn.kduck.core.web.json.JsonObject;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

    private JsonObject getValidErrorJsonObject(List<FieldError> fieldErrors) {
        List<ValidError> validErrorList = new ArrayList<>();
        StringBuilder infoBuilder = new StringBuilder();
        for (FieldError fieldError : fieldErrors) {
            validErrorList.add(new ValidError(fieldError.getField(), fieldError.getDefaultMessage()));
            infoBuilder.append(fieldError.getField() + fieldError.getDefaultMessage());
        }

        JsonObject jsonObject = new JsonObject(validErrorList);
        jsonObject.setMessage("数据校验失败：" + infoBuilder);
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
