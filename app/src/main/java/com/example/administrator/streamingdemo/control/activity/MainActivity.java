package com.example.administrator.streamingdemo.control.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.example.administrator.streamingdemo.R;
import com.example.administrator.streamingdemo.control.fragment.ChannelsFragment;
import com.example.administrator.streamingdemo.control.fragment.GamesFragment;
import com.example.administrator.streamingdemo.control.fragment.HomeFragment;
import com.example.administrator.streamingdemo.control.fragment.LiveFragment;
import com.example.administrator.streamingdemo.control.fragment.MoreSettingFragment;
import com.example.administrator.streamingdemo.control.fragment.SettingFragment;
import com.example.administrator.streamingdemo.model.MainTabsPagerAdapter;
import com.example.administrator.streamingdemo.utils.Constants;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getName();

    private Toolbar mToolbar;
    private FloatingActionButton mFab;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private MainTabsPagerAdapter mViewPagerAdapter;

    private Context mcontext;
    private Handler mOpenDialogHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();
    }

    private void initData() {
        mcontext = this;

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mTabLayout = (TabLayout) findViewById(R.id.tabs);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupViewPager(mViewPager);
        mTabLayout.setupWithViewPager(mViewPager);

        mFab.setOnClickListener(this);

        mOpenDialogHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                int id = msg.what;
                switch (id) {
                    case Constants.MESSAGE_START_MORE_SETTING_DIALOG:
                        Log.d(TAG,"MESSAGE_START_MORE_SETTING_DIALOG");
                        MoreSettingFragment settings = MoreSettingFragment.newInstance();
                        settings.show(getSupportFragmentManager(), "more_settings");
                        break;
                }

                super.handleMessage(msg);
            }
        };
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.fab:
                SettingFragment settings = SettingFragment.newInstance(mOpenDialogHandler);
                settings.show(getSupportFragmentManager(), "settings");
                break;
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        mViewPagerAdapter = new MainTabsPagerAdapter(getSupportFragmentManager());
        mViewPagerAdapter.addFragment(new HomeFragment(), "Home");
        mViewPagerAdapter.addFragment(new GamesFragment(), "Games");
        mViewPagerAdapter.addFragment(new ChannelsFragment(), "Channels");
        mViewPagerAdapter.addFragment(new LiveFragment(), "Lives");
        viewPager.setAdapter(mViewPagerAdapter);
    }
}

