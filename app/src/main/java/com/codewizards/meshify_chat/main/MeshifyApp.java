package com.codewizards.meshify_chat.main;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.codewizards.meshify_chat.utils.Constants;

public class MeshifyApp extends Application {

    private static MeshifyApp meshifyApp;

    public static MeshifyApp getInstance() {
        return meshifyApp;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        meshifyApp = this;
        if (Build.VERSION.SDK_INT >= 26) {
            createNotificationChannel();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        String channelName = getString(MeshifyConstants.string.channel_name);
        String channelDescription = getString(MeshifyConstants.string.channel_description);
        NotificationChannel notificationChannel = new NotificationChannel(Constants.NOTIFICATION_CHANNEL, channelName, NotificationManager.IMPORTANCE_DEFAULT);
        notificationChannel.setDescription(channelDescription);
        notificationChannel.enableLights(true);
        ((NotificationManager) getSystemService(NotificationManager.class)).createNotificationChannel(notificationChannel);
    }
}
