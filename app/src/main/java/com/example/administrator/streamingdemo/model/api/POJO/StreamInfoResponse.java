package com.example.administrator.streamingdemo.model.api.POJO;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by linhtruong on 5/18/2016.
 */
public class StreamInfoResponse implements Serializable {

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

        @SerializedName("stream_server")
        private String streamServer;

        @SerializedName("stream_key")
        private String streamKey;

        @SerializedName("public_url")
        private String publicUrl;

        /**
         * @return The streamServer
         */
        public String getStreamServer() {
            return streamServer;
        }

        /**
         * @param streamServer The stream_server
         */
        public void setStreamServer(String streamServer) {
            this.streamServer = streamServer;
        }

        /**
         * @return The streamKey
         */
        public String getStreamKey() {
            return streamKey;
        }

        /**
         * @param streamKey The stream_key
         */
        public void setStreamKey(String streamKey) {
            this.streamKey = streamKey;
        }

        /**
         * @return The publicUrl
         */
        public String getPublicUrl() {
            return publicUrl;
        }

        /**
         * @param publicUrl The public_url
         */
        public void setPublicUrl(String publicUrl) {
            this.publicUrl = publicUrl;
        }

    }
}
