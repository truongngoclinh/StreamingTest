package com.example.administrator.streamingdemo.control.core.api.network;

import com.example.administrator.streamingdemo.model.api.adapter.APIServerConnectorAdapter;
import com.example.administrator.streamingdemo.model.utils.Results;

/**
 * Created by linhtruong on 5/18/2016.
 */
public class APINetworkManager {

    APINetworkListener mAPINetworkListener;
    APIServerConnectorAdapter mAPIAdapter;

    public APINetworkManager(APINetworkListener listener) {
        mAPINetworkListener = listener;
        mAPIAdapter = new APIServerConnectorAdapter();
    }

    public int authenticate(String email, String password) {
        mAPIAdapter.makeAuthentication(email, password, mAPINetworkListener);
        return Results.SUCCESS;
    }

    public int getStreamInfo(String apiToken) {
        mAPIAdapter.getStreamInfo(apiToken, mAPINetworkListener);
        return Results.SUCCESS;
    }

    public int startStream(String apiToken, String title, String description, int isArchiving, int isMakeArchieve, int isLiveChat, int restriction) {
        mAPIAdapter.startStream(apiToken, title, description, isArchiving, isMakeArchieve, isLiveChat, restriction, mAPINetworkListener);
        return Results.SUCCESS;
    }

    public int stopStream(String apiToken) {
        mAPIAdapter.stopStream(apiToken, mAPINetworkListener);
        return Results.SUCCESS;
    }
}
