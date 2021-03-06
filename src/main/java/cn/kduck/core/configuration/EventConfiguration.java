package cn.kduck.core.configuration;

import cn.kduck.core.event.EventListener;
import cn.kduck.core.event.EventMessageListener;
import cn.kduck.core.event.EventPublisher;
import cn.kduck.core.event.LocalEventBus;
import cn.kduck.core.event.RemoteEventBus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    public static class RemoteEventConfiguration {

        private final Log logger = LogFactory.getLog(getClass());

        private List<EventListener> eventListenerList;

        @Bean
        @ConditionalOnBean(EventListener.class)
        public MessageListener messageListener(){
            return new EventMessageListener(eventListenerList);
        }

        @Bean
        @ConditionalOnBean(EventListener.class)
        public SimpleMessageListenerContainer simpleMessageListenerContainer(ConnectionFactory connectionFactory){
            SimpleMessageListenerContainer messageListenerContainer = new SimpleMessageListenerContainer(connectionFactory);
            messageListenerContainer.setMessageListener(messageListener());
            for (EventListener eventListener : eventListenerList) {
                String queueName = "kduckQueue." + eventListener.eventCode();
                messageListenerContainer.addQueues(new Queue(queueName));

            }
            return messageListenerContainer;
        }

        public RemoteEventConfiguration(ObjectProvider<EventListener> objectProvider){
            List<EventListener> eventListenerList = new ArrayList<>(objectProvider.stream().collect(Collectors.toList()));
            this.eventListenerList = eventListenerList;
        }

        @Bean
        public EventPublisher remoteEventBus(){
            logger.info("Event bus class :" + RemoteEventBus.class.getName());
            return new RemoteEventBus(eventListenerList);
        }
    }

}
