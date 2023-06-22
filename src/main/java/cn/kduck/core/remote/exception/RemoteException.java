package cn.kduck.core.remote.exception;

public class RemoteException extends RuntimeException{

    private final String serviceName;

    public RemoteException(String serviceName) {
        this.serviceName = serviceName;
    }

    public RemoteException(String serviceName,String message) {
        super(message);
        this.serviceName = serviceName;
    }

    public RemoteException(String serviceName,String message, Throwable cause) {
        super(message, cause);
        this.serviceName = serviceName;
    }

    public RemoteException(String serviceName,Throwable cause) {
        super(cause);
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
}
