package com.example.administrator.streamingdemo.model;

/**
 * Created by linhtruong on 5/18/2016.
 * Saved user's info
 */
public class BasicInfo {

    private static BasicInfo mInstance;

    public static BasicInfo getInstance() {
        if (mInstance == null) {
            mInstance = new BasicInfo();
        }
        return mInstance;
    }

    private String apitoken;
    private String streamServer;
    private String streamKey;

    private StreamSettingInfo streamInfo;

    public StreamSettingInfo getStreamInfo() {
        return streamInfo;
    }

    public void setStreamInfo(StreamSettingInfo streamInfo) {
        this.streamInfo = streamInfo;
    }

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

    private String publicUrl;

    public String getApitoken() {
        return apitoken;
    }

    public void setApitoken(String apitoken) {
        this.apitoken = apitoken;
    }

}