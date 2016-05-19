package com.example.administrator.streamingdemo.control;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.example.administrator.streamingdemo.control.core.api.service.APIService;

/**
 * Created by linhtruong on 5/18/2016.
 */
public class BaseApplication extends Application {

    private static final String TAG = BaseApplication.class.getName();

    private static BaseApplication mInstance;
    private static APIService mAPIService;
    private static Context mContext;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mAPIService = ((APIService.APIServiceBinder) service).getApiService();
            Log.d(TAG, "Service is connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mAPIService = null;
            Log.d(TAG, "Service is disconnected");
        }
    };

    public static BaseApplication getInstance() {
        return mInstance;
    }

    public APIService getService() {
        if (mAPIService == null) {
            Log.d(TAG, "Service is still null");
        }
        return mAPIService;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        mContext = getApplicationContext();
        mInstance = this;
        Intent i = new Intent(mInstance, APIService.class);
        bindService(i, mConnection, BIND_AUTO_CREATE);
        Log.d(TAG, "bindService");
    }
}
