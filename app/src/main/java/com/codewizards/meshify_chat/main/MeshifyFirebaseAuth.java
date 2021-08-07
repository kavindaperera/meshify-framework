package com.codewizards.meshify_chat.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.codewizards.meshify_chat.util.Constants;
import com.google.firebase.auth.FirebaseAuth;

public class MeshifyFirebaseAuth implements FirebaseAuth.AuthStateListener, FirebaseAuth.IdTokenListener {

    private static String TAG = "[MeshifyFirebaseAuth]";

    private Context context;

    private FirebaseAuth firebaseAuth;

    protected SharedPreferences settings;

    public MeshifyFirebaseAuth(Context context) {
        this.context = context;
        FirebaseAuth instance = FirebaseAuth.getInstance();
        this.firebaseAuth = instance;
        this.firebaseAuth.addIdTokenListener(this);
        this.firebaseAuth.addAuthStateListener(this);
        this.settings = context.getSharedPreferences(Constants.PREFS_NAME, 0);
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        Log.e(TAG, "onAuthStateChanged:" + firebaseAuth.getCurrentUser().getPhoneNumber());
    }

    @Override
    public void onIdTokenChanged(@NonNull FirebaseAuth firebaseAuth) {

    }
}
