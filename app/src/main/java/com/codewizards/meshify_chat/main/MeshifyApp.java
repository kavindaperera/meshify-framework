package com.codewizards.meshify_chat.main;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.codewizards.meshify_chat.auth.MeshifySession;
import com.codewizards.meshify_chat.util.Constants;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class MeshifyApp extends Application {

    private static MeshifyApp meshifyApp;

    public static MeshifyApp getInstance() {
        return meshifyApp;
    }

    SharedPreferences sharedPreferences;

    MeshifyFirebaseAuth meshifyFirebaseAuth;

    @Override
    public void onCreate() {
        super.onCreate();
        this.sharedPreferences = getApplicationContext().getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        meshifyApp = this;
        FirebaseApp firebaseApp = FirebaseApp.initializeApp(getApplicationContext());
        Objects.requireNonNull(firebaseApp);
        FirebaseAuth.getInstance(firebaseApp);
        meshifyFirebaseAuth = new MeshifyFirebaseAuth(getApplicationContext());

        if (this.sharedPreferences.getLong(Constants.PREFS_FIRST_DATE, 0) == 0) {
            this.sharedPreferences.edit().putLong(Constants.PREFS_FIRST_DATE, System.currentTimeMillis()).apply();
        }

        if (Build.VERSION.SDK_INT >= 26) {
            createNotificationChannel();
        }

        MeshifySession.loadSession(this);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        String channelName = getString(MeshifyConstants.string.channel_name);
        String channelDescription = getString(MeshifyConstants.string.channel_description);
        NotificationChannel notificationChannel = new NotificationChannel(Constants.NOTIFICATION_CHANNEL, channelName, NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.setDescription(channelDescription);
        notificationChannel.enableLights(true);
        ((NotificationManager) getSystemService(NotificationManager.class)).createNotificationChannel(notificationChannel);
    }
}
