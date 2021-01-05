package cn.kduck.core.event;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class LocalEventBus implements EventPublisher{

    @Autowired(required = false)
    private List<EventListener> listenerList;

    private EventTrigger eventTrigger = new EventTrigger();

    public LocalEventBus(List<EventListener> listenerList){
        this.listenerList = listenerList;
    }

    public void publish(Event event) {
        if(listenerList == null){
            return;
        }

        for (EventListener eventListener : listenerList) {
            if(eventListener.eventCode().equals(event.getCode())
                    && eventListener.eventType().equals(event.getType())){

                eventTrigger.onEvent(event, eventListener);
            }
        }
    }

}
