package com.codewizards.meshify.framework.controllers;

import android.content.Context;
import android.content.SharedPreferences;

import com.codewizards.meshify.client.Config;
import com.codewizards.meshify.client.ConfigProfile;
import com.codewizards.meshify.client.Device;
import com.codewizards.meshify.client.Meshify;
import com.codewizards.meshify.client.Message;
import com.codewizards.meshify.client.MessageListener;
import com.codewizards.meshify.client.ConnectionListener;
import com.codewizards.meshify.framework.controllers.connection.ConnectionManager;
import com.codewizards.meshify.framework.controllers.helper.RetryWhenLambda;
import com.codewizards.meshify.framework.entities.MeshifyEntity;
import com.codewizards.meshify.framework.expections.MessageException;
import com.codewizards.meshify.logs.Log;

import java.io.IOException;

import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MeshifyCore {

    /*Shared Preference Keys*/
    public static final String PREFS_NAME = "com.codewizards.meshify.client";
    public static final String PREFS_USER_UUID = "com.codewizards.meshify.uuid";
    public static final String PREFS_APP_KEY = "com.codewizards.meshify.APP_KEY";
    public static final String PREFS_PUBLIC_KEY ="com.codewizards.meshify.key.public";
    public static final String PREFS_PRIVATE_KEY ="com.codewizards.meshify.key.private";
    public static final String PREFS_KEY_PAIRS ="com.codewizards.meshify.key.pairs";

    private static final String TAG = "[Meshify][MeshifyCore]";

    private SharedPreferences sharedPreferences;

    private SharedPreferences.Editor editor;

    private Context context;

    private Config config;

    private MessageListener messageListener;

    private MessageController messageController;

    private MeshifyReceiver meshifyReceiver;

    private ConnectionListener connectionListener;

    private Completable completable = Completable.create(completableEmitter -> {

        if (SessionManager.getSessions().isEmpty()) {
            Log.w(TAG, "all sessions cleaned:");
            completableEmitter.onComplete();
        } else {
            Log.i(TAG, "active connections: ");
            completableEmitter.tryOnError((Throwable) new Exception("Active Connections Found"));
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


    public void shutdownServices() {
        Log.d(TAG, "shutdownServices:");
        this.meshifyReceiver.unregisterReceiver(this.context);
        this.meshifyReceiver.removeAllSessions(this.config.getAntennaType());
        this.meshifyReceiver.stopDiscovery(this.config.getAntennaType());
        this.meshifyReceiver.stopServer(this.config.getAntennaType());
        ConnectionManager.reset();

        Disposable disposable = this.completable.retryWhen(new RetryWhenLambda(3, 500)).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.newThread()).subscribe(()->{
            if (this.getConnectionListener()!=null) {
                Meshify.getInstance().setMeshifyCore(null);
            }
        }, throwable -> Log.e(TAG, "accept: error " + throwable.getMessage()));

    }

    public void pauseServices() {

    }

    public void resumeServices() {

    }

    public void sendMessage(Message message, String receiverId, ConfigProfile profile) {
        Device device = DeviceManager.getDeviceByUserId(receiverId);
        this.messageController.sendMessage(this.context, message, device, profile);
    }

    public void sendBroadcastMessage(Message message, ConfigProfile profile) {
//        profile = profile == null ? ConfigProfile.Default : profile;
        this.messageController.sendMessage( message, profile);
    }

    public MessageListener getMessageListener() {
        Log.d(TAG, "getMessageListener:");
        return this.messageListener;
    }

    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public void setConnectionListener(ConnectionListener connectionListener) {
        this.connectionListener = connectionListener;
    }

    public Context getContext() {
        return this.context;
    }

    public ConnectionListener getConnectionListener() {
        return this.connectionListener;
    }

    public MessageController getMessageController() {
        return this.messageController;
    }

    public void connectDevice(Device device) {
        Log.i(TAG, "Connect to Device: " + device );
        ConnectionManager.connect(device);
    }

    public void disconnectDevice(Device device) {
        Log.i(TAG, "Disconnect Device: " + device);
        Session session = SessionManager.getSession(device.getDeviceAddress());
        if (session != null) {
            session.disconnect();
        }
    }

}
