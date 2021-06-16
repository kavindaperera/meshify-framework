package com.codewizards.meshify.framework.controllers;

import android.content.Context;

import com.codewizards.meshify.client.Config;
import com.codewizards.meshify.client.Device;
import com.codewizards.meshify.logs.Log;

import io.reactivex.Flowable;
import io.reactivex.subscribers.DisposableSubscriber;

abstract class Discovery {

    protected String TAG = "[Meshify][Discovery]";

    DisposableSubscriber<Device> disposableSubscriber;

    Flowable<Device> deviceFlowable; //emits device objects

    private Config config;

    private boolean discoveryRunning = false;

    Discovery() {
    }

    void startDiscovery(Context context, Config config) {
        Log.d(TAG, "startDiscovery:");
        this.disposableSubscriber = new ConnectionSubscriber();
        this.deviceFlowable.subscribe(this.disposableSubscriber); //subscribe to the connection
    }

    void stopDiscovery(Context context) {
        Log.d(TAG, "stopDiscovery:");
        if (this.disposableSubscriber != null) {
            this.disposableSubscriber.dispose();
        }
    }

    public boolean isDiscoveryRunning() {
        return discoveryRunning;
    }

    public void setDiscoveryRunning(boolean discoveryRunning) {
        this.discoveryRunning = discoveryRunning;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    protected void removeDeviceByAntennaType(Config.Antenna antenna) {
        //TODO
    }


}
