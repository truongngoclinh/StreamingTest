package com.example.administrator.streamingdemo.core.api.event;

import com.example.administrator.streamingdemo.model.api.POJO.AuthenticationResponse;
import com.example.administrator.streamingdemo.model.api.POJO.StreamInfoResponse;
import com.example.administrator.streamingdemo.model.api.event.AuthenticationEvent;
import com.example.administrator.streamingdemo.model.api.event.BaseEvent;
import com.example.administrator.streamingdemo.model.api.event.StreamInfoEvent;
import com.example.administrator.streamingdemo.utils.EventType;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by linhtruong on 5/18/2016.
 * Using eventbus to receive/send event to subscriber
 * Declare as Singleton
 */
public class EventManager {

    private static EventManager mInstance;
    private EventBus mEventBus;

    private EventManager() {
        mEventBus = EventBus.getDefault();
    }

    public static EventManager getInstance() {
        if (mInstance == null) {
            mInstance = new EventManager();
        }
        return mInstance;
    }

    public void sendEventAuthenticateStarted(int resultCode) {
        AuthenticationEvent event = new AuthenticationEvent();
        event.setEventType(EventType.AUTHENTICATE_STARTED);
        event.setResultCode(resultCode);
        sendEvent(event);
    }

    public void sendEventAuthenticateFinished(int resultCode, AuthenticationResponse response) {
        AuthenticationEvent event = new AuthenticationEvent();
        event.setEventType(EventType.AUTHENTICATE_FINISHED);
        event.setResultCode(resultCode);
        event.setApiToken(response.getValue().getApiToken());
        sendEvent(event);
    }

    public void sendEventGetStreamInfoStarted(int resultCode) {
        StreamInfoEvent event = new StreamInfoEvent();
        event.setEventType(EventType.GET_STREAM_INFO_STARTED);
        event.setResultCode(resultCode);
        sendEvent(event);
    }

    public void sendEventGetStreamInfoFinished(int resultCode, StreamInfoResponse response) {
        StreamInfoEvent event = new StreamInfoEvent();
        event.setEventType(EventType.GET_STREAM_INFO_FINISHED);
        event.setResultCode(resultCode);
        event.setStreamKey(response.getValue().getStreamKey());
        event.setStreamServer(response.getValue().getStreamServer());
        event.setPublicUrl(response.getValue().getPublicUrl());
        sendEvent(event);
    }

    public void sendEventStartStreamStarted(int resultCode) {
        BaseEvent event = new BaseEvent();
        event.setEventType(EventType.START_STREAMING_STARTED);
        event.setResultCode(resultCode);
        sendEvent(event);
    }

    public void sendEventStartStreamFinished(int resultCode) {
        BaseEvent event = new BaseEvent();
        event.setEventType(EventType.START_STREAMING_FINISHED);
        event.setResultCode(resultCode);
        sendEvent(event);
    }

    private void sendEvent(BaseEvent event) {
        mEventBus.post(event);
    }

}
