package cn.kduck.core.remote.web;

public class RemoteParam<T> {

    private String paramName;
    private T paramValue;

    public RemoteParam(String paramName,T paramValue){
        this.paramName = paramName;
        this.paramValue = paramValue;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public T getParamValue() {
        return paramValue;
    }

    public void setParamValue(T paramValue) {
        this.paramValue = paramValue;
    }
}
