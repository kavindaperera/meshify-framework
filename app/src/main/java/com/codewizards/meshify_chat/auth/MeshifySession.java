package com.codewizards.meshify_chat.auth;

import android.content.Context;
import android.content.SharedPreferences;

import com.codewizards.meshify_chat.util.Constants;

public class MeshifySession {

    private static MeshifySession instance;

    private String uuid;

    private final String username;

    private String phone;

    private SharedPreferences preferences;

    private boolean isRegistered = false;

    private MeshifySession(String username, boolean z) {
        this.username = username;
        this.isRegistered = z;
    }


    private MeshifySession(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PREFS_NAME, 0);
        this.preferences = sharedPreferences;
        this.uuid = this.preferences.getString(Constants.PREFS_USER_UUID, (String) null);
        this.username = this.preferences.getString(Constants.PREFS_USERNAME, (String) null);
        this.phone = this.preferences.getString(Constants.PREFS_USER_PHONE, (String) null);
    }

    public static void loadSession(Context context) {
        instance = new MeshifySession(context);
        if (isLoggedIn()) {
            //TODO
        }
    }

    public static boolean isVerified() {
        String str = instance.phone;
        return str != null && !str.trim().equals("");
    }

    public static void setPhoneNumber(String phoneNumber) {
        instance.phone = phoneNumber;
    }

    public static boolean isLoggedIn() {
        return instance.username != null; //change to uuid
    }

    public static boolean isRegistered() {
        return instance.isRegistered;
    }

    public static String getUuid() {
        return instance.uuid;
    }

    public static String getUsername() {
        return instance.username;
    }

    public static void setSession(Context context, String username) {
        instance = new MeshifySession(username, isRegistered());
        SharedPreferences.Editor edit = context.getSharedPreferences(Constants.PREFS_NAME, 0).edit();
        edit.putString(Constants.PREFS_USERNAME, username);
        edit.commit();
    }
}
