package cn.kduck.core.web.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;
import java.util.Map;

@Configuration
@ConditionalOnProperty(prefix = "kduck.websocket",value="enabled",havingValue = "true")
@EnableWebSocketMessageBroker
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

    @Autowired(required = false)
    private WebSocketAuthentication webSocketAuthentication;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/ws");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/kduck-websocket").withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        if(webSocketAuthentication != null) {
            registration.interceptors(createUserInterceptor(webSocketAuthentication));
        }

    }

//    @Bean
//    @ConditionalOnMissingBean(WebSocketAuthentication.class)
//    public WebSocketAuthentication webSocketAuthentication(){
//        return (Message<?> message, MessageChannel channel) ->null;
//    }

    private ChannelInterceptor createUserInterceptor(WebSocketAuthentication webSocketAuthentication){
        return new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    Object raw = message.getHeaders().get(SimpMessageHeaderAccessor.NATIVE_HEADERS);
//					Object wsSessionId = message.getHeaders().get(SimpMessageHeaderAccessor.SESSION_ID_HEADER);
//					Object userName = message.getHeaders().get(SimpMessageHeaderAccessor.USER_HEADER);
                    accessor.getSessionId();
                    accessor.getUser();
                    if (raw instanceof Map) {
                        Object name = ((Map) raw).get("name");
                        if (name instanceof List) {
//                            accessor.setUser(new WsUser(((List) name).get(0).toString(),accessor.getSessionId()));
							accessor.setUser(webSocketAuthentication.authenticate(message,channel));
                        }
                    }
                }
                return message;
            }
        };
    }
}
