package com.codewizards.meshify.framework.controllers;

import android.content.Context;
import android.content.SharedPreferences;

import com.codewizards.meshify.client.Config;
import com.codewizards.meshify.client.ConfigProfile;
import com.codewizards.meshify.client.Device;
import com.codewizards.meshify.client.Message;
import com.codewizards.meshify.client.MessageListener;
import com.codewizards.meshify.client.StateListener;
import com.codewizards.meshify.framework.entities.MeshifyEntity;
import com.codewizards.meshify.framework.expections.MessageException;
import com.codewizards.meshify.logs.Log;

import java.io.IOException;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;

public class MeshifyCore {

    /*Shared Preference Keys*/
    public static final String PREFS_NAME = "com.codewizards.meshify.client";
    public static final String PREFS_USER_UUID = "com.codewizards.meshify.uuid";
    public static final String PREFS_API_KEY = "com.codewizards.meshify.API_KEY";
    public static final String PREFS_PUBLIC_KEY ="com.codewizards.meshify.key.public";
    public static final String PREFS_PRIVATE_KEY ="com.codewizards.meshify.key.private";

    private static final String TAG = "[Meshify][MeshifyCore]";

    private SharedPreferences sharedPreferences;

    private SharedPreferences.Editor editor;

    private Context context;

    private Config config;

    private MessageListener messageListener;

    private MessageController messageController;

    private MeshifyReceiver meshifyReceiver;

    private StateListener stateListener;

    private Completable completable = Completable.create(completableEmitter -> {

        if (SessionManager.getSessions().isEmpty()) {
            Log.w(TAG, "sessions are cleaned up:");
            completableEmitter.onComplete();
        } else {
            Log.i(TAG, "connections are still active: ");
            completableEmitter.tryOnError((Throwable)new Exception("Connections are still active"));
        }

    });

    public MeshifyCore(Context context, Config config) {
        this.context = context;
        this.config = config;
        this.sharedPreferences = context.getSharedPreferences(MeshifyCore.PREFS_NAME, 0);;
        this.editor = sharedPreferences.edit();
        this.messageController = new MessageController(context, config);
        this.meshifyReceiver = new MeshifyReceiver(config, context);
    }

    public SharedPreferences getSharedPreferences() {
        return this.sharedPreferences;
    }

    public SharedPreferences.Editor getEditor() {
        return this.editor;
    }

    static void sendEntity(Session session, MeshifyEntity meshifyEntity) throws MessageException, IOException {
        Log.d(TAG, "sendEntity:" + meshifyEntity );
        TransactionManager.sendEntity(session, meshifyEntity);
    }


    public void initializeServices() {
        Log.d(TAG, "initializeServices:");
        this.meshifyReceiver.registerReceiver(this.context);
        this.meshifyReceiver.startServer(this.config.getAntennaType());
        this.meshifyReceiver.startDiscovery(this.config.getAntennaType());
    }

    public void sendMessage(Message message, String receiverId, ConfigProfile profile) {
        Device device = DeviceManager.getDeviceByUserId(receiverId);
        this.messageController.sendMessage(this.context, message, device, profile);
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
        return this.stateListener;
    }


    public MessageController getMessageController() {
        return this.messageController;
    }

}
