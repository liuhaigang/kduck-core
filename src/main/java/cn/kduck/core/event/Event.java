package cn.kduck.core.event;

import java.io.Serializable;

public class Event<T> implements Serializable {

    private String code;
    private EventType type;
    private T eventObject;

    Event(){}

    public Event(String code,T eventObject){
        this.code = code;
        this.type = EventType.NONE;
        this.eventObject = eventObject;
    }

    public Event(String code,EventType type,T eventObject){
        this.code = code;
        this.type = type;
        this.eventObject = eventObject;
    }

    public String getCode() {
        return code;
    }

    public EventType getType() {
        return type;
    }

    public void setEventObject(T eventObject) {
        this.eventObject = eventObject;
    }

    public T getEventObject() {
        return eventObject;
    }

    public enum EventType {
        ADD,UPDATE,DELETE,GET,LIST,NONE;
    }

//    public static class EventMessage<T> implements Serializable {
//
//        private T eventObject;
//        private EventType type;
//
//        EventMessage(){}
//
//        public EventMessage(T eventObject, EventType type){
//            this.eventObject = eventObject;
//            this.type = type;
//        }
//
//        public T getEventObject() {
//            return eventObject;
//        }
//
//        public EventType getType() {
//            return type;
//        }
//    }
}
