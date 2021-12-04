package com.codewizards.meshify_chat.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.codewizards.meshify.client.Message;
import com.codewizards.meshify_chat.R;
import com.codewizards.meshify_chat.main.MeshifyApp;
import com.codewizards.meshify_chat.main.MeshifyConstants;
import com.codewizards.meshify_chat.ui.chat.ChatActivity;
import com.codewizards.meshify_chat.util.Constants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class MeshifyNotifications {

    public static String TAG = "[Meshify][MeshifyNotifications]";

    private static CopyOnWriteArrayList<MessageHolder> notifications;

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

    public static void cancelNotifications() {

    }


    public static Bundle prepareMessageBundle(Message message, String str) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.OTHER_USER_NAME, str);
        bundle.putString(Constants.OTHER_USER_ID, message.getSenderId());
        bundle.putString(Constants.MESSAGE_UUID, message.getUuid());
        bundle.putString(Constants.MESSAGE, (String) message.serialize());
        return bundle;
    }


    public void createChatNotification(String senderId, Message message, String str) {
        int i;
        Bundle bundle = new Bundle();
        if (this.sharedPreferences.getBoolean(Constants.PREFS_NOTIFICATION_ENABLED, true)) {

            Intent intent = new Intent().setAction(Constants.CHAT_MESSAGE_RECEIVED_BACKGROUND).setClass(MeshifyApp.getInstance(), ChatActivity.class);

            Message create = message; // TODO - Bundle messages - get from bundle
            String string = senderId; // TODO - Bundle messages - get from bundle
            Iterator<MessageHolder> it = notifications.iterator();

            while (true) {
                if (!it.hasNext()) {
                    i = -1;
                    break;
                }
                MessageHolder next = it.next();
                if (next.getUserId().equals(string)) {
                    next.getMessages().add(create);
                    bundle.putParcelableArrayList(Constants.NOTIFICATION_MESSAGES_ARRAY, next.getMessages());
                    i = next.getNotificationId();
                    bundle.putInt(Constants.NOTIFICATION_ID, i);
                    displayNotification(
                            i,
                            intent.putExtras(bundle).putExtra(Constants.INTENT_EXTRA_NAME, str).putExtra(Constants.INTENT_EXTRA_UUID, string),
                            str);
                    break;
                }
            }

            if (i == -1) {
                int nextInt = new Random().nextInt(10000);
                bundle.putInt(Constants.NOTIFICATION_ID, nextInt);
                if (string == null) {
                    Log.e(TAG, "parameter was null: senderId: " + string);
                }
                ArrayList<Message> arr = new ArrayList<>();
                arr.add(create);
                notifications.add(new MessageHolder(string, arr, nextInt));
                bundle.putParcelableArrayList(Constants.NOTIFICATION_MESSAGES_ARRAY, arr);
                displayNotification(
                        nextInt,
                        intent.putExtras(bundle).putExtra(Constants.INTENT_EXTRA_NAME, str).putExtra(Constants.INTENT_EXTRA_UUID, string),
                        str);
            }
        }
    }

    private synchronized void displayNotification(int i, Intent intent, String str) {
        intent.getExtras().remove(Constants.MESSAGE);
        PendingIntent activity = PendingIntent.getActivity(MeshifyApp.getInstance().getBaseContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        ArrayList parcelableArrayList = intent.getExtras().getParcelableArrayList(Constants.NOTIFICATION_MESSAGES_ARRAY);
        Bitmap largeIcon = BitmapFactory.decodeResource(MeshifyApp.getInstance().getResources(), MeshifyConstants.drawable.mf_launcher);
        NotificationCompat.Builder contentIntent = new NotificationCompat.Builder((Context) MeshifyApp.getInstance(), Constants.NOTIFICATION_CHANNEL)
                .setContentTitle(str)
                .setColor(MeshifyApp.getInstance().getResources().getColor(R.color.colorPrimary))
                .setContentText((String) ((Message) parcelableArrayList.get(parcelableArrayList.size() - 1)).getContent().get("text"))
                .setSmallIcon(MeshifyConstants.drawable.mf)
                .setPriority(4)
                .setLights(MeshifyApp.getInstance().getResources().getColor(R.color.colorPrimary), 1000, 3000)
                .setOnlyAlertOnce(true)
                .setAutoCancel(true)
                .setSound(Uri.parse(this.sharedPreferences.getString(Constants.PREFS_NOTIFICATION_SOUND, RingtoneManager.getDefaultUri(2).toString())))
                .setDefaults(2)
                .setContentIntent(activity);

        if (Build.VERSION.SDK_INT >= 21) {
            contentIntent.setLargeIcon(largeIcon);
        }

//        Person.Builder builder = new Person.Builder();
//        if (str == null) {
//            str = Constants.IMAGES_FOLDER;
//        }
//        builder.setName(str);

//        NotificationCompat.MessagingStyle messagingStyle = new NotificationCompat.MessagingStyle(str);
//        Iterator it = parcelableArrayList.iterator();
//        while (it.hasNext()) {
//            Message message = (Message) it.next();
//            messagingStyle.addMessage(new NotificationCompat.MessagingStyle.Message( message.getDateSent(), builder.build()));
//        }
//        contentIntent.setStyle(messagingStyle);

        ((NotificationManager) MeshifyApp.getInstance().getSystemService(Context.NOTIFICATION_SERVICE)).notify(1, contentIntent.build());

    }

}
