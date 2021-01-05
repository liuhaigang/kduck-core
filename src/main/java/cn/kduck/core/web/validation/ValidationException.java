package cn.kduck.core.web.validation;

import cn.kduck.core.web.validation.ValidError.ValidErrorField;

import java.util.List;

public class ValidationException extends RuntimeException{

    private final List<ValidErrorField> errorFields;

    public ValidationException(String message, List<ValidErrorField> errorFields) {
        super(message);
        this.errorFields = errorFields;
    }

    public ValidationException(String message, List<ValidErrorField> errorFields, Throwable cause) {
        super(message, cause);
        this.errorFields = errorFields;
    }

    public ValidationException(List<ValidErrorField> errorFields,Throwable cause) {
        super(cause);
        this.errorFields = errorFields;
    }

    public List<ValidErrorField> getErrorFields() {
        return errorFields;
    }

//    @Override
//    public String toString() {
//
//        return "ValidationException{" +
//                "errorFields=" + errorFields +
//                '}';
//    }
}
