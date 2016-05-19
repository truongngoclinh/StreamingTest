package com.example.administrator.streamingdemo.control.ui.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;


import com.example.administrator.streamingdemo.R;
import com.example.administrator.streamingdemo.control.core.encoder.ScreenRecorder;
import com.example.administrator.streamingdemo.control.core.service.HeadViewService;

import java.io.File;

/**
 * Combine front camera and screen recorder
 * Make front camera like chat head
 * Using outputBuffer of MediaCodec (H264 bitstream)
 * Encode bitstream to flv, packet rtmp and send to server
 */
public class StreamingScreenActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE = 1;
    private MediaProjectionManager mMediaProjectionManager;
    private ScreenRecorder mRecorder;
    private Button mBtnRecording, mBtnStreaming, mBtnShowFrontCamera;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streaming_screen);
        mContext = this;

        mBtnRecording = (Button) findViewById(R.id.button);
        mBtnStreaming = (Button) findViewById(R.id.btnStreaming);
        mBtnShowFrontCamera = (Button) findViewById(R.id.btnShowFrontCamera);

        mBtnRecording.setOnClickListener(this);
        mBtnShowFrontCamera.setOnClickListener(this);
        mBtnStreaming.setOnClickListener(this);

        mMediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        MediaProjection mediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
        if (mediaProjection == null) {
            return;
        }

        // video size
        final int width = 1280;
        final int height = 720;
        File file = new File(Environment.getExternalStorageDirectory(),

                "record-" + width + "x" + height + "-" + System.currentTimeMillis() + ".mp4");
        final int bitrate = 6000000;
        mRecorder = new ScreenRecorder(width, height, bitrate, 1, mediaProjection, file.getAbsolutePath());
        mRecorder.start();

        Snackbar.make(findViewById(android.R.id.content), "Screen recorder is running...", Snackbar.LENGTH_LONG).show();
        moveTaskToBack(true);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                if (mBtnRecording.getText().equals("Stop")) {
                    if (mRecorder != null) {
                        mRecorder.quit();
                        mRecorder = null;
                    }
                    mBtnRecording.setText("Start");
                } else {
                    mBtnRecording.setText("Stop");
                    Intent captureIntent = mMediaProjectionManager.createScreenCaptureIntent();
                    startActivityForResult(captureIntent, REQUEST_CODE);
                }
                break;
            case R.id.btnStreaming:
                break;
            case R.id.btnShowFrontCamera:
                if (mBtnShowFrontCamera.getText().equals("Start front camera")) {
                    startHeadViewService();
                    mBtnShowFrontCamera.setText("Hide front camera");
                    finish();
                } else {
                    stopHeadViewService();
                    mBtnShowFrontCamera.setText("Start front camera");
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRecorder != null) {
            mRecorder.quit();
            mRecorder = null;
        }
    }

    private void startHeadViewService() {
        mContext.startService(new Intent(mContext, HeadViewService.class));
    }

    private void stopHeadViewService() {
        mContext.stopService(new Intent(mContext, HeadViewService.class));
    }
}
