package com.codewizards.meshify.framework.controllers;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.ParcelUuid;
import android.os.Parcelable;

import com.codewizards.meshify.client.Config;
import com.codewizards.meshify.client.Device;
import com.codewizards.meshify.client.MeshifyUtils;
import com.codewizards.meshify.logs.Log;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

    private CopyOnWriteArrayList<BluetoothDevice> discoveredDevices;

    static String l;

    static String m;

    private FlowableEmitter<Device> emitter;

    public BluetoothDiscovery(Context context) {
        Log.d(TAG, "BluetoothDiscovery:constructor");
        this.context = context;
        this.bluetoothAdapter = MeshifyUtils.getBluetoothAdapter(context);
        this.confirmedBluetoothDevices = new CopyOnWriteArrayList<>();
        this.devices = new CopyOnWriteArrayList<>();
        this.discoveredDevices = new CopyOnWriteArrayList<>();

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
            })/*.retryWhen(TODO-implement a flatMap Function)*/.subscribe(new CompletableObserver(){

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

    @SuppressLint("MissingPermission")
    private void sdpSearch(BluetoothDevice bluetoothDevice) {
        try {
            Log.v(TAG, "... fetching with sdpSearch " + bluetoothDevice.getAddress() + " (" + bluetoothDevice.getName() + ")");
            bluetoothDevice.getClass().getDeclaredMethod("sdpSearch", new Class[]{ParcelUuid.class}).invoke(bluetoothDevice, new Object[]{new ParcelUuid(BluetoothUtils.getBluetoothUuid())});
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    void DiscoveryFinishedAction(Context context) {
        Log.i(this.TAG, "DiscoveryFinishedAction: ");
        this.setDiscoveryRunning(false);
        this.devices.clear();
        if (this.bluetoothAdapter.isEnabled()) {
            if (!this.discoveredDevices.isEmpty()) {

                Log.v(TAG, "fetchUuidsWithSdp() from " + this.discoveredDevices.size() + " discovered devices:");
                for (BluetoothDevice bluetoothDevice : this.discoveredDevices) {
                    new ScheduledThreadPoolExecutor(1).schedule(() -> {
                        this.removeDevice(bluetoothDevice);
                        Log.w(TAG, "removed device due to fetch timeout: " + bluetoothDevice.getAddress());
                    }, 20000L, TimeUnit.MILLISECONDS);
//                    if (Build.VERSION.SDK_INT > 23) {
                        Log.v(TAG, "fetching " + bluetoothDevice.getAddress() + " (" + bluetoothDevice.getName() + ")");
                        bluetoothDevice.fetchUuidsWithSdp();
                        continue;
//                    }
//                    this.sdpSearch(bluetoothDevice);
                }
                this.startDiscovery(context, this.getConfig());
            } else {
                this.startDiscovery(context, this.getConfig());
            }
        }
    }

    @SuppressLint(value = {"HardwareIds", "MissingPermission"})
    void addBluetoothDevice(Intent intent) {

        BluetoothDevice bluetoothDevice = (BluetoothDevice)intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        Log.d(TAG, "addBluetoothDevice: " + bluetoothDevice.getName());

        boolean z = true;

        if (bluetoothDevice != null && bluetoothDevice.getAddress() != null && bluetoothDevice.getName() != null && bluetoothDevice.getBluetoothClass() != null) {
            if (bluetoothDevice.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.PHONE_SMART) {
                if (!this.confirmedBluetoothDevices.contains(bluetoothDevice)) {
                    Device device = DeviceManager.getDevice(bluetoothDevice.getAddress());
                    if (device == null || device.getSessionId() == null) {
                        z = false;
                    }
                } else {
                    Log.v(TAG, "Previously known Meshify user.");
                }

                this.addDevice(bluetoothDevice, z, false);

            }
        }

    }

    private void addDevice(BluetoothDevice bluetoothDevice, boolean z, boolean isBLE) {
        Log.d(TAG, "addDevice: isKnown:" + z + " | isBle:" + isBLE);

        Device device = new Device(bluetoothDevice, isBLE);
        this.devices.addIfAbsent(device);

        if (z) {
            DeviceManager.addDevice(device);
            this.addIfAbsentConfirmed(bluetoothDevice);
            this.emitter.onNext(device); //notify connection subscriber

        } else if (!z && !isBLE) {
            this.addIfAbsentDiscovered(bluetoothDevice);
        }

    }

    void addIfAbsentConfirmed(BluetoothDevice bluetoothDevice) {
        Log.d(TAG, "addDevice: " + bluetoothDevice + " to confirmedBluetoothDevices" );
        this.confirmedBluetoothDevices.addIfAbsent(bluetoothDevice);
    }

    private void addIfAbsentDiscovered(BluetoothDevice bluetoothDevice) {
        Log.d(TAG, "addDevice: " + bluetoothDevice + " to discoveredDevices" );
        this.discoveredDevices.addIfAbsent(bluetoothDevice);
    }

    private boolean removeDevice(BluetoothDevice bluetoothDevice) {
        return this.discoveredDevices.remove((Object)bluetoothDevice);
    }

    private void removeDiscoveredDevice(BluetoothDevice bluetoothDevice) {
        this.removeDevice(bluetoothDevice);
        if (this.discoveredDevices.size() == 0) {
            this.startDiscovery(this.context, this.getConfig());
        }
    }

    @SuppressLint("MissingPermission")
    void pair(Intent intent){
        BluetoothDevice bluetoothDevice = (BluetoothDevice)intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        Parcelable[] arrparcelable = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
        if (bluetoothDevice != null && bluetoothDevice.getAddress() != null) {
            if (arrparcelable != null) {
                boolean matched = false;
                for (Parcelable parcelable : arrparcelable) {
                    Log.v(this.TAG, "::: ::: ::: FETCHED UUID: " + parcelable.toString() + " Device: " + bluetoothDevice.getName() + " Address: " + bluetoothDevice.getAddress());
                    if (!(parcelable.toString().equals(BluetoothUtils.getBluetoothUuid().toString()))) continue;
                    Log.v(this.TAG, "::: ::: ::: Matching device found!");
                    matched = true;
                }
                this.addDevice(bluetoothDevice, matched, false);
            } else {
                Log.v(this.TAG, "::: ::: ::: Received null UUIDs from Device: " + bluetoothDevice.getName() + "Address: " + bluetoothDevice.getAddress());
            }
        }
        this.removeDiscoveredDevice(bluetoothDevice);
    }

}
