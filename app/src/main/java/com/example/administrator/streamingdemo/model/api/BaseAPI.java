package com.example.administrator.streamingdemo.model.api;

import com.example.administrator.streamingdemo.model.api.POJO.AuthenticationResponse;
import com.example.administrator.streamingdemo.model.api.POJO.BaseResponse;
import com.example.administrator.streamingdemo.model.api.POJO.StreamInfoResponse;

import retrofit.Call;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by linhtruong on 5/18/2016.
 */
public interface BaseAPI {

    /**
     * authentication, get api_token
     */
    @FormUrlEncoded
    @POST("login")
    Call<AuthenticationResponse> authenticate(@Field("email") String email, @Field("password") String password);

    /**
     * get stream url/key
     */
    @FormUrlEncoded
    @POST("get")
    Call<StreamInfoResponse> getStreamInfo(@Field("api_token") String apiToken);

    /**
     * post stream's title/description..., notify start streaming
     * For int case:
     * value 1 => true
     * value 0 => false
     */
    @FormUrlEncoded
    @POST("start")
    Call<BaseResponse> startStream(
            @Field("api_token") String apiToken,
            @Field("name") String title,
            @Field("description") String description,
            @Field("archiving") int isArchiving,
            @Field("make_archive") int isMakeArchive,
            @Field("live_chat") int isLiveChat,
            @Field("restriction") int restriction);


    /**
     * stop streaming
     */
    @FormUrlEncoded
    @POST("stop")
    Call<BaseResponse> stopStream(@Field("api_token") String apiToken);
}
