package com.example.administrator.streamingdemo.control.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.administrator.streamingdemo.R;
import com.example.administrator.streamingdemo.control.BaseApplication;
import com.example.administrator.streamingdemo.control.core.api.service.APIService;
import com.example.administrator.streamingdemo.control.core.encoder.CameraToH264;
import com.example.administrator.streamingdemo.model.BasicInfo;
import com.example.administrator.streamingdemo.model.api.event.AuthenticationEvent;
import com.example.administrator.streamingdemo.model.api.event.StreamInfoEvent;
import com.example.administrator.streamingdemo.model.utils.Constants;
import com.example.administrator.streamingdemo.model.utils.EventType;
import com.example.administrator.streamingdemo.model.utils.Results;
import com.example.administrator.streamingdemo.model.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Created by linhtruong on 5/17/2016.
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = LoginActivity.class.getName();

    private EditText mEdtUsername;
    private EditText mEdtPassword;

    private Button mBtnForgotPw;
    private Button mBtnLogin;

    private Handler mHandler;
    private ProgressDialog mProgressDialog;
    private APIService mAPIService;
    private BasicInfo mBasicInfo;

    Thread authenticateThread = new Thread(new Runnable() {
        @Override
        public void run() {
          /*  while (BaseApplication.getInstance().getService() == null) {
                // wait
            }*/
            mAPIService = BaseApplication.getInstance().getService();
            String email = mEdtUsername.getText().toString();
            String password = mEdtPassword.getText().toString();
            if (Utils.isStringHasText(email) && Utils.isStringHasText(password)) {
                mAPIService.authenticate(mEdtUsername.getText().toString(), mEdtPassword.getText().toString());
            } else {
                Snackbar.make(findViewById(R.id.contentLayout), "Wrong email or password!", Snackbar.LENGTH_LONG).show();
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        setContentView(R.layout.activity_login);

        initData();

        // register eventBus
        EventBus.getDefault().register(this);
    }

    private void initData() {
        mEdtUsername = (EditText) findViewById(R.id.edtUsername);
        mEdtPassword = (EditText) findViewById(R.id.edtPW);
        mBtnForgotPw = (Button) findViewById(R.id.btnForgotPw);
        mBtnLogin = (Button) findViewById(R.id.btnLogin);

        mBtnLogin.setOnClickListener(this);
        mBtnForgotPw.setOnClickListener(this);

        mBasicInfo = BasicInfo.getInstance();
        mHandler = new Handler();

        /* set default email & pw to test */
        mEdtUsername.setText(Constants.DEFAULT_ACCOUNT_EMAIL);
        mEdtPassword.setText(Constants.DEFAULT_ACCOUNT_PASSWORD);

        mProgressDialog = new ProgressDialog(LoginActivity.this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setMessage(getString(R.string.loginProgress));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btnForgotPw:
                CameraToH264 cameraEncoder = new CameraToH264();
                try {
                    cameraEncoder.testEncodeCameraToMp4();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    Log.d(TAG, "Encoding error = " + throwable.getMessage());
                }
                break;

            case R.id.btnLogin:

           /*     // test no api
                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);*/

//                authenticateThread.start();
                mAPIService = BaseApplication.getInstance().getService();
                String email = mEdtUsername.getText().toString();
                String password = mEdtPassword.getText().toString();
                if (Utils.isStringHasText(email) && Utils.isStringHasText(password)) {
                    mAPIService.authenticate(mEdtUsername.getText().toString(), mEdtPassword.getText().toString());
                } else {
                    Snackbar.make(findViewById(R.id.contentLayout), "Wrong email or password!", Snackbar.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Subscribe
    public void onEvent(final AuthenticationEvent event) {
        int type = event.getEventType();
        switch (type) {
            case EventType.AUTHENTICATE_STARTED:
                Log.d(TAG, "AUTHENTICATE_STARTED");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (event.getResultCode() == Results.SUCCESS) {
                            mProgressDialog.show();
                        }
                    }
                });
                break;

            case EventType.AUTHENTICATE_FINISHED:
                Log.d(TAG, "AUTHENTICATE_FINISHED");
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
                if (event.getResultCode() == Results.SUCCESS) {
                    final String apiToken = event.getApiToken();
                    mBasicInfo.setApitoken(apiToken);
                    mAPIService.getStreamInfo(apiToken);
                    Snackbar.make(findViewById(R.id.contentLayout), "Login successful, get stream info...", Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(findViewById(R.id.contentLayout), "Login failed!", Snackbar.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Subscribe
    public void onEvent(final StreamInfoEvent event) {
        int type = event.getEventType();
        switch (type) {
            case EventType.GET_STREAM_INFO_STARTED:
                Log.d(TAG, "GET_STREAM_INFO_STARTED");
                break;

            case EventType.GET_STREAM_INFO_FINISHED:
                Log.d(TAG, "GET_STREAM_INFO_FINISHED");
                if (event.getResultCode() == Results.SUCCESS) {
                    mBasicInfo.setPublicUrl(event.getPublicUrl());
                    mBasicInfo.setStreamServer(event.getStreamServer());
                    mBasicInfo.setStreamKey(event.getStreamKey());

                    Intent i = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(i);
                } else {
                    Snackbar.make(findViewById(R.id.contentLayout), "Get stream info failed!", Snackbar.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mProgressDialog = null;
    }
}
