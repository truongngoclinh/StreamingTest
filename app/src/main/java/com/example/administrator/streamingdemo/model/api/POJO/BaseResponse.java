package com.example.administrator.streamingdemo.model.api.POJO;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by linhtruong on 5/18/2016.
 */
public class BaseResponse implements Serializable {

    @SerializedName("error")
    private String error;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
