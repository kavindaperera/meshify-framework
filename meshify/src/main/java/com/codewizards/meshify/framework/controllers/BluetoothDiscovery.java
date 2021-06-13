package com.codewizards.meshify.framework.controllers;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Build;
import android.os.ParcelUuid;

import com.codewizards.meshify.client.Config;
import com.codewizards.meshify.client.Device;
import com.codewizards.meshify.client.MeshifyUtils;
import com.codewizards.meshify.logs.Log;

import java.util.concurrent.CopyOnWriteArrayList;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.subscribers.DisposableSubscriber;

public class BluetoothDiscovery extends Discovery {

    protected String TAG = "[Meshify][BluetoothDiscovery]";

    Context context;

    private BluetoothAdapter bluetoothAdapter;

    private CopyOnWriteArrayList<BluetoothDevice> confirmedBluetoothDevices; //CopyOnWriteArrayList is a thread-safe variant of ArrayList

    private CopyOnWriteArrayList<Device> devices;

    private CopyOnWriteArrayList<BluetoothDevice> bluetoothDevices;

    static String l;

    static String m;

    private FlowableEmitter<Device> emitter;

    public BluetoothDiscovery(Context context) {
        Log.d(TAG, "BluetoothDiscovery:constructor");
        this.context = context;
        this.bluetoothAdapter = MeshifyUtils.getBluetoothAdapter(context);
        this.confirmedBluetoothDevices = new CopyOnWriteArrayList<>();
        this.devices = new CopyOnWriteArrayList<>();
        this.bluetoothDevices = new CopyOnWriteArrayList<>();

        if (Build.VERSION.SDK_INT >= 23) { //check whether sdp search is available
            try {
                l = (String) BluetoothDevice.class.getDeclaredField("ACTION_SDP_RECORD").get(null);
                m = (String) BluetoothDevice.class.getDeclaredField("EXTRA_SDP_SEARCH_STATUS").get(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        this.deviceFlowable = Flowable.create(flowableEmitter -> { //emits the discovered devices
            this.emitter = flowableEmitter;
        }, (BackpressureStrategy)BackpressureStrategy.BUFFER); //BackpressureStrategy.BUFFER, the source will buffer all the events until the subscriber can consume them

    }


    @SuppressLint("MissingPermission")
    @Override
    public void startDiscovery(Context context, Config config) {
        Log.d(TAG, "startDiscovery:");
        super.startDiscovery(context, config);
        this.setConfig(config);
        if (!this.bluetoothAdapter.isDiscovering()) {
            Completable.create(completableEmitter -> {
                if (!this.bluetoothAdapter.startDiscovery()) {
                    completableEmitter.tryOnError(new Throwable("Discovery start failed"));
                } else {
                    completableEmitter.onComplete();
                }
            })/*.retryWhen(TODO-implement a flatMap)*/.subscribe(new CompletableObserver(){

                public void onSubscribe(Disposable d2) {
                    Log.d(TAG, "onSubscribe:");
                }

                public void onComplete() {
                    Log.d(TAG, "onComplete:");
                }

                public void onError(Throwable e2) {
                    Log.d(TAG, "onError:" + e2);
                    BluetoothDiscovery.this.stopDiscovery(context);
                }
            });
        }
    }


}
