package cn.kduck.core.configuration;

import cn.kduck.core.event.EventListener;
import cn.kduck.core.event.EventMessageListener;
import cn.kduck.core.event.EventPublisher;
import cn.kduck.core.event.LocalEventBus;
import cn.kduck.core.event.RemoteEventBus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static cn.kduck.core.event.RemoteEventBus.KDUCK_EVENT_EXCHANGE_NAME;

/**
 * LiuHG
 */
public class EventConfiguration {

    @Configuration
    @ConditionalOnMissingClass("org.springframework.amqp.rabbit.connection.ConnectionFactory")
    public static class LocalEventConfiguration {

        private final Log logger = LogFactory.getLog(getClass());

        private List<EventListener> eventListenerList;

        public LocalEventConfiguration(ObjectProvider<EventListener> objectProvider){
            List<EventListener> eventListenerList = new ArrayList<>(objectProvider.stream().collect(Collectors.toList()));
            this.eventListenerList = eventListenerList;
        }

        @Bean
        public EventPublisher localEventBus(){
            logger.info("Event bus class :" + LocalEventBus.class.getName());
            return new LocalEventBus(eventListenerList);
        }
    }

    @Configuration
    @ConditionalOnClass(name="org.springframework.amqp.rabbit.connection.ConnectionFactory")
    public static class RemoteEventConfiguration implements InitializingBean {

        private final Log logger = LogFactory.getLog(getClass());
        private final ConnectionFactory connectionFactory;
        private final AmqpAdmin amqpAdmin;

        private List<EventListener> eventListenerList;

        public RemoteEventConfiguration(ConnectionFactory connectionFactory, AmqpAdmin amqpAdmin,ObjectProvider<EventListener> objectProvider){
            List<EventListener> eventListenerList = new ArrayList<>(objectProvider.stream().collect(Collectors.toList()));
            this.eventListenerList = eventListenerList;
            this.connectionFactory = connectionFactory;
            this.amqpAdmin = amqpAdmin;
        }

//        @Bean
//        @ConditionalOnBean(EventListener.class)
//        public MessageListener messageListener(){
//            return new EventMessageListener(eventListenerList);
//        }

//        @Bean
//        @ConditionalOnBean(EventListener.class)
        public void initMessageListeners(){
            if(eventListenerList.isEmpty()){
                return;
            }
            amqpAdmin.declareExchange(new TopicExchange(KDUCK_EVENT_EXCHANGE_NAME));

            for (EventListener eventListener : eventListenerList) {
                EventMessageListener eventMessageListener = new EventMessageListener(eventListener);
                SimpleMessageListenerContainer messageListenerContainer = new SimpleMessageListenerContainer(connectionFactory);
                messageListenerContainer.setMessageListener(eventMessageListener);

                String md5 = DigestUtils.md5DigestAsHex(eventListener.getClass().getName().getBytes());
                String key = eventListener.eventCode() + "." + eventListener.eventType();
                String queueName = "kduckQueue." + key + "." + md5;

                Queue queue = new Queue(queueName);
                amqpAdmin.declareQueue(queue);
                amqpAdmin.declareBinding(new Binding(queueName, DestinationType.QUEUE,KDUCK_EVENT_EXCHANGE_NAME,key + ".#",null));

                messageListenerContainer.addQueues(queue);
                messageListenerContainer.start();


            }

//            SimpleMessageListenerContainer messageListenerContainer = new SimpleMessageListenerContainer(connectionFactory);
//            messageListenerContainer.setMessageListener(messageListener());
//
//            for (EventListener eventListener : eventListenerList) {
////                String md5 = DigestUtils.md5DigestAsHex(eventListener.getClass().getName().getBytes());
//                //FIXME
//                String key = eventListener.eventCode() + "." + eventListener.eventType();
//                String queueName = "kduckQueue." + key;
//                Queue queue = new Queue(queueName);
//                messageListenerContainer.addQueues(queue);
//                amqpAdmin.declareQueue(queue);
//            }
//
//            messageListenerContainer.start();
//            return messageListenerContainer;
        }

//        private String getIpAdress(){
//            InetAddress address = null;
//            try {
//                address = InetAddress.getLocalHost();
//            } catch (UnknownHostException e) {
//                throw new RuntimeException("获取服务器IP错误",e);
//            }
//            return address.getHostAddress();
//        }



        @Bean
        public EventPublisher remoteEventBus(){
            logger.info("Event bus class :" + RemoteEventBus.class.getName());
//            return new RemoteEventBus(eventListenerList);
            return new RemoteEventBus();
        }

        @Override
        public void afterPropertiesSet() throws Exception {
            initMessageListeners();
        }
    }

}
