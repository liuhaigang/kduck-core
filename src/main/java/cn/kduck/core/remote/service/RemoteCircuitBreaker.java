package cn.kduck.core.remote.service;

import cn.kduck.core.remote.exception.RemoteException;
import cn.kduck.core.web.json.JsonObject;

public interface RemoteCircuitBreaker {

    JsonObject fallback(RemoteException e);
}
