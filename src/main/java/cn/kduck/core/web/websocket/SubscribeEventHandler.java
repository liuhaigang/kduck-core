package cn.kduck.core.web.websocket;

import java.security.Principal;

public interface SubscribeEventHandler {

    void onSubscribe(Principal principal, String sessionId);
}
