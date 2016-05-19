package com.example.administrator.streamingdemo.control.controller;

import com.example.administrator.streamingdemo.core.api.event.EventManager;
import com.example.administrator.streamingdemo.core.api.network.APINetworkListener;
import com.example.administrator.streamingdemo.core.api.network.APINetworkManager;
import com.example.administrator.streamingdemo.model.BasicInfo;
import com.example.administrator.streamingdemo.model.api.POJO.AuthenticationResponse;
import com.example.administrator.streamingdemo.model.api.POJO.BaseResponse;
import com.example.administrator.streamingdemo.model.api.POJO.StreamInfoResponse;
import com.example.administrator.streamingdemo.model.api.event.AuthenticationEvent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by linhtruong on 5/18/2016.
 */
public class APIController implements APINetworkListener {

    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.MINUTES;
    private static final int NUMBER_OF_CORE = Runtime.getRuntime().availableProcessors();
    private static final int MAX_POOL_SIZE = 4 * NUMBER_OF_CORE;

    private APINetworkManager mAPINetworkManager;
    private EventManager mEventManager;
    private ThreadPoolExecutor mThreadPoolExecutor;
    private BlockingQueue<Runnable> mBlockingQueue;

    public APIController() {
        mBlockingQueue = new LinkedBlockingDeque<>();
        mThreadPoolExecutor = new ThreadPoolExecutor(NUMBER_OF_CORE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, mBlockingQueue);
        mAPINetworkManager = new APINetworkManager(this);
        mEventManager = EventManager.getInstance();
    }

    public void getAuthenticate(final String email, final String password) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                int result = mAPINetworkManager.authenticate(email, password);
                mEventManager.sendEventAuthenticateStarted(result);
            }
        };
        doTask(task);
    }

    public void getStreamInfo(final String apiToken) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                int result = mAPINetworkManager.getStreamInfo(apiToken);
                mEventManager.sendEventGetStreamInfoStarted(result);
            }
        };
        doTask(task);
    }

    public void startStream(final String title, final String description, final int isArchiving, final int isMakeArchieve, final int isLiveChat,
                            final int restriction) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                int result = mAPINetworkManager.startStream(title, description, isArchiving, isMakeArchieve, isLiveChat, restriction);
                mEventManager.sendEventAuthenticateStarted(result);
            }
        };
        doTask(task);
    }


    @Override
    public void onGetAuthenticationFinished(final int resultCode, final AuthenticationResponse authenticationResponse) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                mEventManager.sendEventAuthenticateFinished(resultCode, authenticationResponse);
            }
        };
        doTask(task);
    }

    @Override
    public void onGetStreamInfoFinished(final int resultCode, final StreamInfoResponse streamInfoResponse) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                mEventManager.sendEventGetStreamInfoFinished(resultCode, streamInfoResponse);
            }
        };
        doTask(task);
    }

    @Override
    public void onStartStreamFinished(final int resultcode, final BaseResponse baseResponse) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                mEventManager.sendEventStartStreamFinished(resultcode);
            }
        };
        doTask(task);
    }

    private void doTask(Runnable task) {
        mThreadPoolExecutor.execute(task);
    }
}
