package com.codewizards.meshify_chat.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.codewizards.meshify_chat.auth.MeshifySession;
import com.codewizards.meshify_chat.util.Constants;
import com.firebase.ui.auth.data.model.PhoneNumber;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class MeshifyFirebaseAuth implements FirebaseAuth.AuthStateListener, FirebaseAuth.IdTokenListener {

    private static String TAG = "[MeshifyFirebaseAuth]";

    private Context context;

    private FirebaseAuth firebaseAuth;

    protected SharedPreferences settings;

    private PhoneNumber phoneNumber;

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
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            String phoneNumber = firebaseUser.getPhoneNumber();
            if (phoneNumber!=null && !phoneNumber.trim().equals("")){
                MeshifySession.setPhoneNumber(phoneNumber);
                this.settings.edit().putString(Constants.PREFS_USER_PHONE, phoneNumber).apply();
            }
        }


    }

    @Override
    public void onIdTokenChanged(@NonNull FirebaseAuth firebaseAuth) {

    }
}
