package cn.kduck.core.remote.web;

import cn.kduck.core.remote.service.RemoteServiceDepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/proxy")
public class RemoteController {

    private ObjectMapper objectMapper = new ObjectMapper();

    private static Map<String,Method> serviceMethodCache = new HashMap();

    @PostMapping("/{serviceName}")
    public Object doCall(@PathVariable("serviceName") String serviceName, @RequestBody RemoteMethod remoteMethod){
        //从缓存里根据methodName方法命名规则获取到对应的实例的Method对象（缓存Method），依次反序列化方法参数对于选罢法，
        // 并调用对应的方法并返回出去

        Assert.notNull(remoteMethod.getMethodName(),"远程调用方法名不能为null");

        Object serviceObject = RemoteServiceDepository.getServiceObject(serviceName);
        Method method = serviceMethodCache.get(serviceName + "#" + remoteMethod.getMethodName());
        if(method == null){
            Method[] methods = serviceObject.getClass().getMethods();
            for (Method method1 : methods) {
                String methodName = methodName(method1);
                if(methodName.equals(remoteMethod.getMethodName())){
                    method = method1;
                    serviceMethodCache.put(serviceName + "#" + remoteMethod.getMethodName(),method1);
                    break;
                }
            }
        }

        if(method == null){
            throw new RuntimeException("在" + serviceObject.getClass() + "中没有匹配的方法"+remoteMethod.getMethodName());
        }

        Class<?>[] parameterTypes = method.getParameterTypes();//接口方法参数类型列表
        Object[] paramJsons = remoteMethod.getParams();//远程传递的接口方法参数json
        Object[] args = new Object[parameterTypes.length];//接口参数
        for (int i = 0; i < args.length; i++) {

            if(paramJsons[i] == null){
                args[i] = null;
                continue;
            }

            Class componentType = parameterTypes[i];
            boolean isArray = false;
            if(parameterTypes[i].isArray()){
                componentType = parameterTypes[i].getComponentType();
                isArray = true;
            }

            if(componentType.isInterface()){
                if(isArray){
                    parameterTypes[i] = String[].class;
                }else{
                    parameterTypes[i] = paramJsons[i].getClass();
                }

            }

            try {
                Object obj = objectMapper.readValue(paramJsons[i].toString(), parameterTypes[i]);
                args[i] = obj;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            return method.invoke(serviceObject,args);
        } catch (Exception e) {
            throw new RuntimeException("调用远程接口异常",e);
        }
    }


    private String methodName(Method method){
        StringBuilder methodNameBuilder = new StringBuilder(method.getName());
        methodNameBuilder.append("(");
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class parameterType = parameterTypes[i];
            if(i  != 0){
                methodNameBuilder.append(",");
            }
            methodNameBuilder.append(parameterType.getSimpleName());
        }
        methodNameBuilder.append(")");
        return methodNameBuilder.toString();
    }
}
