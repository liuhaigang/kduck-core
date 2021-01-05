package cn.kduck.core.remote.web;

public class RemoteMethod {

//    private String className;
    private String methodName;

    private Object[] params;

    public RemoteMethod(){}

    public RemoteMethod(String className,String methodName){
        this.methodName = methodName;
    }


    public RemoteMethod(String className,String methodName,Object[] params){
        this.methodName = methodName;
        this.params = params;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    //    public List<RemoteParam> getParamList() {
//        return paramList;
//    }
//
//    public void setParamList(List<RemoteParam> paramList) {
//        this.paramList = paramList;
//    }
}
