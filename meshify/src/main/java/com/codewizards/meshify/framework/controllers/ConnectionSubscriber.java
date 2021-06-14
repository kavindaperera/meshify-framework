package com.codewizards.meshify.framework.controllers;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;

import com.codewizards.meshify.client.Config;
import com.codewizards.meshify.client.Device;
import com.codewizards.meshify.client.Meshify;
import com.codewizards.meshify.client.MeshifyUtils;
import com.codewizards.meshify.logs.Log;

import io.reactivex.CompletableObserver;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;

public class ConnectionSubscriber extends DisposableSubscriber<Device> {

    private static final String TAG = "[Meshify][Connection_Subscriber]";

    public ConnectionSubscriber() {
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart: ");
        super.onStart();
        request(1L);
    }

    @Override
    public void onNext(Device device) {
        Log.d(TAG, "onNext: device: " + device + " | onThread: " +  Thread.currentThread().getName());

        MeshifyDevice meshifyDevice = ConnectionManager.getConnectivity(device);

        if (meshifyDevice != null) {
            CompletableObserver completableObserver = new CompletableObserver() {
                @Override
                public void onSubscribe(@NonNull Disposable d) {
                    Log.d(TAG, "onSubscribe:MeshifyDevice ");
                }

                @Override
                public void onComplete() {
                    Log.d(TAG, "onComplete:MeshifyDevice ");
                    ConnectionManager.setMeshifyDevice((MeshifyDevice) null);
                    ConnectionSubscriber.this.request(1L);
                }

                @Override
                public void onError(@NonNull Throwable e) {
                    Log.e(TAG, "onError:MeshifyDevice ");
                    MeshifyDevice meshifyDevice = ConnectionManager.getMeshifyDevice();
                    if (meshifyDevice != null && meshifyDevice.getDevice().equals(device)) {
                        if (meshifyDevice instanceof BluetoothMeshifyDevice) {
                            ConnectionSubscriber.this.accept(meshifyDevice.getDevice());
                        }
                        ConnectionManager.setMeshifyDevice((MeshifyDevice) null);
                        ConnectionSubscriber.this.request(1L);
                    }
                }
            };

            if (device.getAntennaType() == Config.Antenna.BLUETOOTH_LE) {

                //TODO - subscribe

            } else {
                meshifyDevice.create().subscribeOn(Schedulers.newThread()).subscribe(completableObserver); //completableObserver subscribes meshifyDevice on a new thread
            }

        } else {
            request(1L);
        }

    }

    @SuppressLint("MissingPermission")
    public void accept(Device device) {
        Log.d(TAG, "accept: " + device.getDeviceName());

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
