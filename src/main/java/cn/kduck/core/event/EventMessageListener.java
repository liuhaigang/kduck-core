package cn.kduck.core.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

public class EventMessageListener implements MessageListener {

    private ObjectMapper objectMapper = new ObjectMapper();

    private EventListener eventListener;

    private EventTrigger eventTrigger = new EventTrigger();

    @Autowired
    public EventMessageListener(EventListener eventListener){
        this.eventListener = eventListener;
    }

    @Override
    public void onMessage(Message message) {
        Event event;
        try {
            event = objectMapper.readValue(message.getBody(), Event.class);
        } catch (IOException e) {
            throw new RuntimeException("消息对象转换为Event对象时错误",e);
        }
        eventTrigger.onEvent(event,eventListener);
//        for (EventListener eventListener : eventListenerList) {
//            if(eventListener.eventCode().equals(event.getCode())
//                    && eventListener.eventType().equals(event.getType())){
//                eventTrigger.onEvent(event,eventListener);
//            }
//        }
    }
}
