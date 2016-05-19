package com.example.administrator.streamingdemo.control.core.api.network;

import com.example.administrator.streamingdemo.model.api.POJO.AuthenticationResponse;
import com.example.administrator.streamingdemo.model.api.POJO.BaseResponse;
import com.example.administrator.streamingdemo.model.api.POJO.StreamInfoResponse;

/**
 * Created by linhtruong on 5/18/2016.
 */
public interface APINetworkListener {

    void onGetAuthenticationFinished(int resultCode, AuthenticationResponse authenticationResponse);

    void onGetStreamInfoFinished(int resultCode, StreamInfoResponse streamInfoResponse);

    void onStartStreamFinished(int resultcode, BaseResponse baseResponse);
}
