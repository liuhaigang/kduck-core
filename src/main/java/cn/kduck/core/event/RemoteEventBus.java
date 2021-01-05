package cn.kduck.core.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class RemoteEventBus implements EventPublisher, InitializingBean {

    private final String KDUCK_EVENT_EXCHANGE_NAME = "kduckExchange";

    private ObjectMapper objectMapper = new ObjectMapper();

//    @Autowired(required = false)
//    private SimpleMessageListenerContainer messageListenerContainer;

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private AmqpTemplate amqpTemplate;

    private List<String> routeKeyList = new ArrayList<>();

    @Autowired(required = false)
    private List<EventListener> listenerList;

    public RemoteEventBus(List<EventListener> listenerList){
        this.listenerList = listenerList;
    }

    @Override
    public void publish(Event event) {
        if(listenerList == null){
            return;
        }

        String key = event.getCode() + "." + event.getType();
        if(!routeKeyList.contains(key)){

            String queueName = "kduckQueue." + event.getCode();
            Queue queue = new Queue(queueName);
            amqpAdmin.declareQueue(queue);
            amqpAdmin.declareBinding(new Binding(queueName, DestinationType.QUEUE,KDUCK_EVENT_EXCHANGE_NAME,key,null));

//            if(messageListenerContainer != null){
//                messageListenerContainer.addQueues(queue);
//            }

            routeKeyList.add(key);
        }

        String message;
        try {
            message = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("发送事件，Event对象转换为Json时出现错误",e);
        }
        amqpTemplate.convertAndSend(KDUCK_EVENT_EXCHANGE_NAME,key,message);

    }

    @Override
    public void afterPropertiesSet() throws Exception {
//        if(amqpTemplate instanceof RabbitTemplate){
//            ((RabbitTemplate)amqpTemplate).setMessageConverter(new Jackson2JsonMessageConverter());
//        }
        amqpAdmin.declareExchange(new TopicExchange(KDUCK_EVENT_EXCHANGE_NAME));
//        if(listenerList != null){
//            if(amqpTemplate instanceof RabbitTemplate){
//                ((RabbitTemplate)amqpTemplate).setMessageConverter(new Jackson2JsonMessageConverter());
//            }
//            amqpAdmin.declareExchange(new TopicExchange(KDUCK_EVENT_EXCHANGE_NAME));
//            List<String> queueNameList = new ArrayList<>(listenerList.size());
//            for (EventListener eventListener : listenerList) {
//                String key = eventListener.eventCode()+"."+eventListener.eventType();
//                String queueName = "kduckEventQueue." + eventListener.eventCode();
//                amqpAdmin.declareQueue(new Queue(queueName));
//                amqpAdmin.declareBinding(new Binding(queueName, DestinationType.QUEUE,KDUCK_EVENT_EXCHANGE_NAME,key,null));
//                queueNameList.add(queueName);
//            }
//            messageListenerContainer.setQueueNames(queueNameList.toArray(new String[0]));
//        }

    }
}
