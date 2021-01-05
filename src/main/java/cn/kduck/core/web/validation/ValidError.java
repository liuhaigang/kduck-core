package cn.kduck.core.web.validation;

import java.util.Collections;
import java.util.List;

/**
 * LiuHG
 */
public class ValidError {

    private List<ValidErrorField> errorList;

    public ValidErrorField[] getErrorField(){
        if(errorList == null){
            return new ValidErrorField[0];
        }
        return errorList.toArray(new ValidErrorField[0]);
    }

    public boolean hasError(){
        return errorList != null && !errorList.isEmpty();
    }

    public void setErrorList(List<ValidErrorField> errorList){
        if(this.errorList != null){
            throw new UnsupportedOperationException("不能覆盖当前字端验证结果");
        }
        this.errorList = Collections.unmodifiableList(errorList);
    }

    public static class ValidErrorField {
        private String fieldName;
        private String message;

        public ValidErrorField(String fieldName, String message) {
            this.fieldName = fieldName;
            this.message = message;
        }

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
