package com.example.administrator.streamingdemo.model.api.event;

/**
 * Created by linhtruong on 5/18/2016.
 */
public class AuthenticationEvent extends BaseEvent {

    private String apiToken;

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }
}
