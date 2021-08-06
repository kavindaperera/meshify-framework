package com.codewizards.meshify_chat.service;


import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.codewizards.meshify_chat.main.MeshifyConstants;
import com.codewizards.meshify_chat.ui.home.MainActivity;
import com.codewizards.meshify_chat.util.Constants;

import static android.content.Intent.FLAG_RECEIVER_FOREGROUND;
import static com.codewizards.meshify_chat.util.Constants.NOTIFICATION_CHANNEL;

public class MeshifyService extends Service {

    private static final String TAG = "[MeshifyService]";;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), MeshifyConstants.drawable.mf_launcher);
        switch (action) {
            case Constants.MESHIFY_APP_FOREGROUND: {
                meshifyStopForeground();
            }
            case Constants.MESHIFY_APP_BACKGROUND: {
                Intent clickIntent = new Intent(this, MainActivity.class);
                clickIntent.setAction(Constants.MESHIFY_APP_FOREGROUND);
                clickIntent.setFlags(FLAG_RECEIVER_FOREGROUND);
                PendingIntent activity = PendingIntent.getActivity(this, 0,clickIntent,0);
                Intent stopIntent = new Intent(this, MeshifyService.class);
                stopIntent.setAction(Constants.MESHIFY_STOP);
                Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
                        .setContentTitle(getString(MeshifyConstants.string.app_name))
                        .setTicker(getString(MeshifyConstants.string.app_name))
                        .setContentText(getString(MeshifyConstants.string.foreground_notification_content_title))
                        .setSmallIcon(MeshifyConstants.drawable.mf)
                        .setLargeIcon(largeIcon)
                        .setContentIntent(activity)
//                        .setOngoing(true)
                        .setPriority(Notification.PRIORITY_MAX)
                        .addAction(1, getString(MeshifyConstants.string.foreground_notification_action_stop), PendingIntent.getService(this, 0, stopIntent, 0))
                        .build();
                startForeground(Constants.FOREGROUND_SERVICE, notification);
                break;
            }
            case Constants.MESHIFY_STOP: {
                meshifyStopForeground();
            }
        }

        return START_NOT_STICKY;

    }

    private void meshifyStopForeground() {
        if (Build.VERSION.SDK_INT >= 24) {
            stopForeground(STOP_FOREGROUND_REMOVE);
        } else {
            stopForeground(true);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
