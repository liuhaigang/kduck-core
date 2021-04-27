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

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Configuration
@ConditionalOnProperty(prefix = "kduck.websocket",value="enabled",havingValue = "true")
@EnableWebSocketMessageBroker
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

    @Autowired(required = false)
    private WebSocketAuthentication webSocketAuthentication;

    @Autowired(required = false)
    private ConnectEventHandler connectEventHandler;

    @Autowired(required = false)
    private DisconnectEventHandler disconnectEventHandler;

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
        registration.interceptors(createUserInterceptor());

    }

    private ChannelInterceptor createUserInterceptor(){
        return new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                String sessionId = accessor.getSessionId();
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    Object nativeHeaders = message.getHeaders().get(SimpMessageHeaderAccessor.NATIVE_HEADERS);
                    Principal user = accessor.getUser();

                    Principal principal;
                    if(webSocketAuthentication != null){
                        principal = webSocketAuthentication.authenticate(user, sessionId, nativeHeaders);
                    }else{
                        principal = user;
                    }
                    if(principal != null){
                        accessor.setUser(principal);
                    }

                    connectEventHandler.onConnect(principal,sessionId);

                }else if(StompCommand.DISCONNECT.equals(accessor.getCommand())){
                    Principal principal = accessor.getUser();
                    disconnectEventHandler.onDisconnect(principal,sessionId);
                }
                return message;
            }
        };
    }
}
