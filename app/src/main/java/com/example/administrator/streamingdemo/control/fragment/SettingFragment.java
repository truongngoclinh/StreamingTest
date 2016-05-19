package com.example.administrator.streamingdemo.control.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.example.administrator.streamingdemo.R;
import com.example.administrator.streamingdemo.control.activity.StreamingAcitivity;
//import com.example.administrator.streamingdemo.control.activity.StreamingCameraActivity;
import com.example.administrator.streamingdemo.model.BasicInfo;
import com.example.administrator.streamingdemo.model.StreamSettingInfo;
import com.example.administrator.streamingdemo.utils.Constants;

import java.util.List;

/**
 * Created by linhtruong on 5/17/2016.
 */
public class SettingFragment extends DialogFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private SwitchCompat mSwtScreen;
    private SwitchCompat mSwtCamera;

    private AppCompatCheckBox mChk720;
    private AppCompatCheckBox mChk480;

    private EditText mEdtTitle;
    private EditText mEdtDescription;

    private Button mBtnCancel;
    private Button mBtnOk;

    private static Handler mOpenDialogHandler;

    public static SettingFragment newInstance(Handler handler) {
        mOpenDialogHandler = handler;
        return new SettingFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View v = inflater.inflate(R.layout.setting_fragment, container, false);
        initData(v);

        return v;
    }

    private void initData(View v) {
        mSwtCamera = (SwitchCompat) v.findViewById(R.id.swtCamera);
        mSwtScreen = (SwitchCompat) v.findViewById(R.id.swtScreen);
        mChk480 = (AppCompatCheckBox) v.findViewById(R.id.chkBox480);
        mChk720 = (AppCompatCheckBox) v.findViewById(R.id.chkBox720);
        mBtnCancel = (Button) v.findViewById(R.id.btnCancel);
        mBtnOk = (Button) v.findViewById(R.id.btnOk);
        mEdtDescription = (EditText) v.findViewById(R.id.edtDescription);
        mEdtTitle = (EditText) v.findViewById(R.id.edtTitle);

        mBtnCancel.setOnClickListener(this);
        mBtnOk.setOnClickListener(this);
        mSwtCamera.setOnCheckedChangeListener(this);
        mSwtScreen.setOnCheckedChangeListener(this);
        mChk480.setOnCheckedChangeListener(this);
        mChk720.setOnCheckedChangeListener(this);

        getDialog().getWindow().getAttributes().windowAnimations = R.anim.exit_left;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btnCancel:
                if (isAdded()) {
                    dismiss();
                }
                break;
            case R.id.btnOk:
                StreamSettingInfo info = new StreamSettingInfo();
                info.setType((mSwtCamera.isChecked() == true) ? Constants.STREAM_TYPE_CAMERA : Constants.STREAM_TYPE_SCREEN);
                info.setQuanlity((mChk480.isChecked() == true) ? Constants.STREAM_QUANLITY_480P : Constants.STREAM_QUANLITY_720P);
                info.setTitle((mEdtTitle.getText().toString()));
                info.setDescription(mEdtDescription.getText().toString());
                BasicInfo.getInstance().setStreamInfo(info);

                mOpenDialogHandler.sendEmptyMessage(Constants.MESSAGE_START_MORE_SETTING_DIALOG);
                dismiss();

                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        /* only accept 1 option */
        switch (id) {
            case R.id.swtCamera:
                if (isChecked) {
                    mSwtScreen.setChecked(false);
                }
                break;

            case R.id.swtScreen:
                if (isChecked) {
                    mSwtCamera.setChecked(false);
                }
                break;

            case R.id.chkBox480:
                if (isChecked) {
                    mChk720.setChecked(false);
                }
                break;

            case R.id.chkBox720:
                if (isChecked) {
                    mChk480.setChecked(false);
                }
                break;
        }
    }
}
