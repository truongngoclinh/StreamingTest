package com.example.administrator.streamingdemo.model.api.POJO;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by linhtruong on 5/18/2016.
 */
public class AuthenticationResponse implements Serializable {

    @SerializedName("error")
    private String error;

    @SerializedName("value")
    private Value value;

    /**
     * @return The error
     */
    public String getError() {
        return error;
    }

    /**
     * @param error The error
     */
    public void setError(String error) {
        this.error = error;
    }

    /**
     * @return The value
     */
    public Value getValue() {
        return value;
    }

    /**
     * @param value The value
     */
    public void setValue(Value value) {
        this.value = value;
    }


    public class Value {

        @SerializedName("api_token")
        private String apiToken;

        /**
         * @return The apiToken
         */
        public String getApiToken() {
            return apiToken;
        }

        /**
         * @param apiToken The api_token
         */
        public void setApiToken(String apiToken) {
            this.apiToken = apiToken;
        }

    }
}
