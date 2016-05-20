package com.example.administrator.streamingdemo.model.api.adapter;

import android.util.Log;

import com.example.administrator.streamingdemo.control.core.api.network.APINetworkListener;
import com.example.administrator.streamingdemo.model.api.BaseAPI;
import com.example.administrator.streamingdemo.model.api.POJO.AuthenticationResponse;
import com.example.administrator.streamingdemo.model.api.POJO.BaseResponse;
import com.example.administrator.streamingdemo.model.api.POJO.StreamInfoResponse;
import com.example.administrator.streamingdemo.model.utils.Results;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;


/**
 * Created by linhtruong on 5/18/2016.
 */
public class APIServerConnectorAdapter {

    private static String TAG = APIServerConnectorAdapter.class.getName();

    final String CONTENT_TYPE = "application/json";
    String AUTHENTICATE_URL = "http://192.168.90.39/api/";

    private Retrofit mRetrofit;
    private BaseAPI mAPI;

    public APIServerConnectorAdapter() {
        mRetrofit = getRetrofit(AUTHENTICATE_URL);
        mAPI = getBaseAPI(mRetrofit);
    }

    /**
     * set header
     */
    public OkHttpClient getRequestHeader() {
        OkHttpClient httpClient = new OkHttpClient();
        httpClient.setConnectTimeout(20, TimeUnit.SECONDS);
        httpClient.setReadTimeout(30, TimeUnit.SECONDS);
        httpClient.interceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request.Builder requestBuilder = chain.request().newBuilder();
                requestBuilder.header("Content-Type", CONTENT_TYPE);
                return chain.proceed(requestBuilder.build());
            }
        });
        return httpClient;
    }

    public void makeAuthentication(String email, String password, final APINetworkListener listener) {
        try {
            Call<AuthenticationResponse> call = mAPI.authenticate(email, password);
            call.enqueue(new Callback<AuthenticationResponse>() {
                @Override
                public void onResponse(retrofit.Response<AuthenticationResponse> response, Retrofit retrofit) {
                    Log.d(TAG, "makeAuthentication: response = " + response.raw());
                    listener.onGetAuthenticationFinished(Results.SUCCESS, response.body());
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.d(TAG, "makeAuthentication: failed = " + t.getMessage());
                    listener.onGetAuthenticationFinished(Results.ERROR, null);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            listener.onGetAuthenticationFinished(Results.ERROR, null);
        }
    }

    public void getStreamInfo(String apitoken, final APINetworkListener listener) {
        try {
            Call<StreamInfoResponse> call = mAPI.getStreamInfo(apitoken);
            call.enqueue(new Callback<StreamInfoResponse>() {
                @Override
                public void onResponse(retrofit.Response<StreamInfoResponse> response, Retrofit retrofit) {
                    Log.d(TAG, "getStreamInfo: response = " + response.raw());
                    listener.onGetStreamInfoFinished(Results.SUCCESS, response.body());
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.d(TAG, "getStreamInfo: failed = " + t.getMessage());
                    listener.onGetStreamInfoFinished(Results.ERROR, null);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            listener.onGetStreamInfoFinished(Results.ERROR, null);
        }
    }

    public void startStream(String apiToken, String title, String description, int isArchiving, int isMakeArchieve, int isLiveChat, int restriction, final APINetworkListener listener) {
        try {
            Call<BaseResponse> call = mAPI.startStream(apiToken, title, description, isArchiving, isMakeArchieve, isLiveChat, restriction);
            call.enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(retrofit.Response<BaseResponse> response, Retrofit retrofit) {
                    Log.d(TAG, "startStream: response = " + response.body());
                    listener.onStartStreamFinished(Results.SUCCESS, response.body());
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.d(TAG, "startStream: failed = " + t.getMessage());
                    listener.onStartStreamFinished(Results.ERROR, null);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            listener.onStartStreamFinished(Results.ERROR, null);
        }
    }

    public void stopStream(String apiToken, final APINetworkListener listener) {
        try {
            Call<BaseResponse> call = mAPI.stopStream(apiToken);
            call.enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(retrofit.Response<BaseResponse> response, Retrofit retrofit) {
                    Log.d(TAG, "startStream: response = " + response.body());
                    listener.onStopStreamFinished(Results.SUCCESS, response.body());
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.d(TAG, "startStream: failed = " + t.getMessage());
                    listener.onStopStreamFinished(Results.ERROR, null);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            listener.onStopStreamFinished(Results.ERROR, null);
        }
    }


    /**
     * get custom retrofit base on URl
     *
     * @param baseUrl
     * @return retrofit
     */
    public Retrofit getRetrofit(String baseUrl) {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(getRequestHeader())
                .build();
        return retrofit;
    }

    /**
     * get retrofit API connector
     *
     * @param retrofit
     * @return API
     */
    public BaseAPI getBaseAPI(Retrofit retrofit) {
        BaseAPI baseAPI = retrofit.create(BaseAPI.class);
        return baseAPI;
    }
}
