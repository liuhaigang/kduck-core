package cn.kduck.core.remote.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ProxyService {

    String serviceName();

    String[] servicePaths() default {};

//    int version();

//    RemoteMode remoteMode() default RemoteMode.BOTH;


//    enum RemoteMode {
//        JUST_CLIENT,
//        JUST_SERVER,
//        BOTH;
//    }
}
