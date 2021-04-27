package cn.kduck.core.web.websocket;

import java.security.Principal;

public interface DisconnectEventHandler {

    void onDisconnect(Principal principal,String sessionId);
}
