package com.codewizards.meshify.framework.controllers;

import android.content.Context;
import android.content.SharedPreferences;

import com.codewizards.meshify.client.Config;
import com.codewizards.meshify.client.Device;
import com.codewizards.meshify.client.MessageListener;
import com.codewizards.meshify.client.StateListener;
import com.codewizards.meshify.logs.Log;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;

public class MeshifyCore {

    /*Shared Preference Keys*/
    public static final String PREFS_NAME = "com.codewizards.meshify.client";
    public static final String PREFS_USER_UUID = "com.codewizards.meshify.uuid";
    public static final String PREFS_API_KEY = "com.codewizards.meshify.API_KEY";
    public static final String PREFS_PUBLIC_KEY ="com.codewizards.meshify.key.public";
    public static final String PREFS_PRIVATE_KEY ="com.codewizards.meshify.key.public";

    private static final String TAG = "[Meshify][MeshifyCore]";

    private SharedPreferences sharedPreferences;

    private SharedPreferences.Editor editor;

    private Context context;

    private Config config;

    private MessageListener messageListener;

    private MeshifyReceiver meshifyReceiver;

    private StateListener stateListener;

    private Completable completable = Completable.create(CompletableEmitter -> {

        //TODO - cleaning the core

    });

    public MeshifyCore(Context context, Config config) {
        Log.d(TAG, "MeshifyCore:");
        this.context = context;
        this.config = config;
        this.sharedPreferences = context.getSharedPreferences(MeshifyCore.PREFS_NAME, 0);;
        this.editor = sharedPreferences.edit();

        this.meshifyReceiver = new MeshifyReceiver(config, context);
    }

    public SharedPreferences getSharedPreferences() {
        Log.d(TAG, "getEditor:");
        return this.sharedPreferences;
    }

    public SharedPreferences.Editor getEditor() {
        Log.d(TAG, "getEditor:");
        return this.editor;
    }

    public void initializeServices() {
        Log.d(TAG, "initializeServices:");
        this.meshifyReceiver.registerReceiver(this.context);
        this.meshifyReceiver.startServer(this.config.getAntennaType());
        this.meshifyReceiver.startDiscovery(this.config.getAntennaType());
    }


    MessageListener getMessageListener() {
        Log.d(TAG, "getMessageListener:");
        return this.messageListener;
    }

    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public void setStateListener(StateListener stateListener) {
        this.stateListener = stateListener;
    }

    public Context getContext() {
        return this.context;
    }

    StateListener getStateListener() {
        Log.d(TAG, "getStateListener:");
        return this.stateListener;
    }



}
