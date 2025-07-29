package cn.kduck.core.event;


import cn.kduck.core.event.Event.EventType;
import cn.kduck.core.event.Event.EventTypeEnum;

public interface EventListener<T> {

    String eventCode();

    default EventType eventType() {
        return EventTypeEnum.NONE;
    }

    void onEvent(T eventObject);
}
