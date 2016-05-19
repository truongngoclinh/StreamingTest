package com.example.administrator.streamingdemo.control.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.example.administrator.streamingdemo.R;
import com.example.administrator.streamingdemo.model.StreamSettingInfo;
import com.example.administrator.streamingdemo.model.utils.Constants;

/**
 * Created by linhtruong on 5/17/2016.
 */
public class StreamingAcitivity extends AppCompatActivity {

    private StreamSettingInfo mStreamInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streaming);

        Intent i = getIntent();
        mStreamInfo = (StreamSettingInfo) i.getSerializableExtra(Constants.STREAM_INFO);
    }
}
