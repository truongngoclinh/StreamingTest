package com.example.administrator.streamingdemo.model.api.event;

/**
 * Created by linhtruong on 5/18/2016.
 */
public class StreamInfoEvent extends BaseEvent {

    private String streamServer;

    private String streamKey;

    private String publicUrl;

    public String getStreamServer() {
        return streamServer;
    }

    public void setStreamServer(String streamServer) {
        this.streamServer = streamServer;
    }

    public String getStreamKey() {
        return streamKey;
    }

    public void setStreamKey(String streamKey) {
        this.streamKey = streamKey;
    }

    public String getPublicUrl() {
        return publicUrl;
    }

    public void setPublicUrl(String publicUrl) {
        this.publicUrl = publicUrl;
    }
}
