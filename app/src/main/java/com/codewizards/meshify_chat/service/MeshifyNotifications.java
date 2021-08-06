package com.codewizards.meshify_chat.service;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.core.app.NotificationCompat;

import com.codewizards.meshify.client.Message;
import com.codewizards.meshify_chat.main.MeshifyApp;
import com.codewizards.meshify_chat.main.MeshifyConstants;
import com.codewizards.meshify_chat.util.Constants;

import java.util.concurrent.CopyOnWriteArrayList;

public class MeshifyNotifications {

    private static CopyOnWriteArrayList<String> notifications;

    private static MeshifyNotifications instance = new MeshifyNotifications();

    SharedPreferences sharedPreferences = MeshifyApp.getInstance().getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);

    private MeshifyNotifications() {
        this.notifications = new CopyOnWriteArrayList<>();
    }

    public static MeshifyNotifications getInstance() {
        if (instance == null) {
            instance = new MeshifyNotifications();
        }
        return instance;
    }

    public void createChatNotification(Message message, String str) {

        if (this.sharedPreferences.getBoolean(Constants.PREFS_NOTIFICATION_ENABLED, true)) {

            Bitmap largeIcon = BitmapFactory.decodeResource(MeshifyApp.getInstance().getResources(), MeshifyConstants.drawable.mf_launcher);

            NotificationCompat.Builder contentIntent = new NotificationCompat.Builder((Context) MeshifyApp.getInstance(), Constants.NOTIFICATION_CHANNEL)
                    .setContentTitle("New Message")
                    .setContentText(str)
                    .setSmallIcon(MeshifyConstants.drawable.mf)
                    .setLargeIcon(largeIcon);

//        Person.Builder builder = new Person.Builder();
//        builder.setName(message.getSenderId());
//        NotificationCompat.MessagingStyle messagingStyle = new NotificationCompat.MessagingStyle(builder.build());
//        contentIntent.setStyle(messagingStyle);

            ((NotificationManager) MeshifyApp.getInstance().getSystemService(Context.NOTIFICATION_SERVICE)).notify(1, contentIntent.build());
        }
    }

}
