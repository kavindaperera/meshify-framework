package com.codewizards.meshify.framework.controllers;

import com.codewizards.meshify.client.Device;
import com.codewizards.meshify.logs.Log;

import io.reactivex.subscribers.DisposableSubscriber;

public class ConnectionSubscriber extends DisposableSubscriber<Device> {

    private static final String TAG = "[Meshify][Connection_Subscriber]";

    public ConnectionSubscriber() {
        Log.d(TAG, "Connection_Subscriber:constructor");
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart: ");
        super.onStart();
        request(1L);
    }

    @Override
    public void onNext(Device device) {

        //TODO - create a completableObserver

    }

    @Override
    public void onError(Throwable t) {
        Log.e(TAG, "onError: " + t.getMessage());
    }

    @Override
    public void onComplete() {
        Log.d(TAG, "onComplete: ");
        if (!isDisposed()) {
            dispose();
        }
    }
}
