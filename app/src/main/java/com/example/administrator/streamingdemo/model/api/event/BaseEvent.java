package com.example.administrator.streamingdemo.model.api.event;

/**
 * Created by linhtruong on 5/18/2016.
 * Specific event should extends this class
 */
public class BaseEvent {

    private int eventType;
    private int resultCode;

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EventType : " + getEventType() + ", ");
        builder.append("ResultCode : " + getResultCode());
        return builder.toString();
    }
}
