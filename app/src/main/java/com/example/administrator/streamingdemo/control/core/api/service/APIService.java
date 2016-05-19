package com.example.administrator.streamingdemo.control.core.api.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.administrator.streamingdemo.control.ui.controller.APIController;

/**
 * Created by linhtruong on 5/18/2016.
 */
public class APIService extends Service {

    private static final String TAG = APIService.class.getName();

    private APIController mAPIController;

    private final IBinder mBinder = new APIServiceBinder();

    public class APIServiceBinder extends Binder {
        public APIService getApiService() {
            return APIService.this;
        }
    }

    private void initialize() {
        mAPIController = new APIController();
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");
        initialize();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()");
        return mBinder;
    }

    public void authenticate(String email, String password) {
        Log.d(TAG, "authenticate()");
        mAPIController.getAuthenticate(email, password);
    }

    public void getStreamInfo(String apiToken) {
        Log.d(TAG, "getStreamInfo()");
        mAPIController.getStreamInfo(apiToken);
    }

    public void startStream(String title, String description, int isArchiving, int isMakeArchieve, int isLiveChat, int restriction) {
        Log.d(TAG, "startStream()");
        mAPIController.startStream(title, description, isArchiving, isMakeArchieve, isLiveChat, restriction);
    }
}
