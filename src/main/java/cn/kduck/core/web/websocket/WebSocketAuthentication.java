package cn.kduck.core.web.websocket;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import java.security.Principal;

public interface WebSocketAuthentication {
    Principal authenticate(Message<?> message, MessageChannel channel);
}