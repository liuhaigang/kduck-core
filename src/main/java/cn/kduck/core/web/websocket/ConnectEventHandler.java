package cn.kduck.core.web.websocket;

import java.security.Principal;

public interface ConnectEventHandler {

    void onConnect(Principal principal, String sessionId);
}
