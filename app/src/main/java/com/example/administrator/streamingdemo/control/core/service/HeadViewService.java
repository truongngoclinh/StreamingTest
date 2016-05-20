package com.example.administrator.streamingdemo.control.core.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.example.administrator.streamingdemo.R;
import com.example.administrator.streamingdemo.control.ui.activity.StreamingScreenActivity;
import com.example.administrator.streamingdemo.view.FrontCameraView;

/**
 * Created by linhtruong on 5/19/2016.
 * Service for start/stop front camera head view (on top of all applications, attached to window manager)
 */
public class HeadViewService extends Service {

    private final static int FOREGROUND_ID = 999;
    private FrontCameraView mFrontCameraView;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service started", Toast.LENGTH_LONG).show();
        mFrontCameraView = new FrontCameraView(this);

        PendingIntent pendingIntent = createPendingIntent();
        Notification notification = createNotification(pendingIntent);
        startForeground(FOREGROUND_ID, notification);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show();
        stopForeground(true);
        mFrontCameraView.destroy();
        mFrontCameraView = null;
    }

    private PendingIntent createPendingIntent() {
        Intent intent = new Intent(this, StreamingScreenActivity.class);
        return PendingIntent.getActivity(this, 0, intent, 0);
    }

    private Notification createNotification(PendingIntent intent) {
        return new Notification.Builder(this)
                .setContentTitle(getText(R.string.notificationTitle))
                .setContentText(getText(R.string.notificationText))
                .setSmallIcon(R.drawable.icon_broadcast)
                .setContentIntent(intent)
                .build();
    }

}
