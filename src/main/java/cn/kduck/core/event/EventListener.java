package cn.kduck.core.event;


import cn.kduck.core.event.Event.EventType;

public interface EventListener<T> {

    String eventCode();

    default EventType eventType() {
        return EventType.NONE;
    }

    void onEvent(T eventObject);
}
