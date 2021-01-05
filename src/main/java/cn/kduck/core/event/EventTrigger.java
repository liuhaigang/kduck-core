package cn.kduck.core.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import cn.kduck.core.service.ValueMap;
import org.springframework.core.ResolvableType;

import java.io.IOException;
import java.util.Map;

public class EventTrigger {

    private ObjectMapper objectMapper = new ObjectMapper();

    public void onEvent(Event event, EventListener eventListener) {
        ResolvableType resolvableType = ResolvableType.forClass(eventListener.getClass());
        ResolvableType[] interfaces = resolvableType.getInterfaces();
        for (ResolvableType interfaceType : interfaces) {
            if(interfaceType.toClass() == EventListener.class){
                Class<?> resolve = interfaceType.getGeneric(0).resolve();
                if(resolve == null){
                    ValueMap valueMap = new ValueMap((Map)event.getEventObject());
                    eventListener.onEvent(valueMap);
                }else{
                    try {
                        String json = objectMapper.writeValueAsString(event.getEventObject());
                        Object eventObject = objectMapper.readValue(json, resolve);
                        eventListener.onEvent(eventObject);
                    } catch (IOException e) {
                        throw new RuntimeException("处理监听消息时发生异常，Json消息对象转换错误",e);
                    }
                }
                break;
            }
        }
    }
}
