package com.example.administrator.streamingdemo.model.utils.lib.sea;

import android.os.Environment;
import android.util.Log;

import com.example.administrator.streamingdemo.model.utils.lib.sea.rtmp.RtmpPublisher;
import com.example.administrator.streamingdemo.model.utils.lib.sea.rtmp.io.RtmpConnection;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Srs implementation of an RTMP publisher
 *
 * @author francois, leoma
 */
public class SrsRtmpPublisher implements RtmpPublisher {

    private RtmpConnection rtmpConnection;
    FileOutputStream outputStream;

    public SrsRtmpPublisher(RtmpPublisher.EventHandler handler) {
        rtmpConnection = new RtmpConnection(handler);
        try {
            outputStream = new FileOutputStream(Environment.getExternalStorageDirectory() + "/test.flv");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connect(String url) throws IOException {
        rtmpConnection.connect(url);
    }

    @Override
    public void shutdown() {
        rtmpConnection.shutdown();
    }

    @Override
    public void publish(String publishType) throws IllegalStateException, IOException {
        if (publishType == null) {
            throw new IllegalStateException("No publish type specified");
        }
        rtmpConnection.publish(publishType);
    }

    @Override
    public void closeStream() throws IllegalStateException {
        rtmpConnection.closeStream();
    }

    @Override
    public void publishVideoData(byte[] data) throws IllegalStateException {
        Log.d("LINH", "data.lenght = " + data.length);
        if (data == null || data.length == 0) {
            throw new IllegalStateException("Invalid Video Data");
        }
        try {
            outputStream.write(data, 0, data.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        rtmpConnection.publishVideoData(data);
    }

    @Override
    public void publishAudioData(byte[] data) throws IllegalStateException {
        if (data == null || data.length == 0) {
            throw new IllegalStateException("Invalid Audio Data");
        }
        rtmpConnection.publishAudioData(data);
    }

    @Override
    public final AtomicInteger getVideoFrameCacheNumber() {
        return rtmpConnection.getVideoFrameCacheNumber();
    }

    @Override
    public final EventHandler getEventHandler() {
        return rtmpConnection.getEventHandler();
    }

    @Override
    public final String getServerIpAddr() {
        return rtmpConnection.getServerIpAddr();
    }

    @Override
    public final int getServerPid() {
        return rtmpConnection.getServerPid();
    }

    @Override
    public final int getServerId() {
        return rtmpConnection.getServerId();
    }

    @Override
    public void setVideoResolution(int width, int height) {
        rtmpConnection.setVideoResolution(width, height);
    }

}
