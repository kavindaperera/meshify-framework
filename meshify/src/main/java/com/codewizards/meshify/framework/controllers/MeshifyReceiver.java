package com.codewizards.meshify.framework.controllers;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import com.codewizards.meshify.client.Config;
import com.codewizards.meshify.client.DeviceProfile;
import com.codewizards.meshify.client.Meshify;
import com.codewizards.meshify.client.MeshifyException;
import com.codewizards.meshify.client.MeshifyUtils;
import com.codewizards.meshify.framework.expections.ConnectionException;
import com.codewizards.meshify.logs.Log;

public class MeshifyReceiver extends BroadcastReceiver {

    private final String TAG = "[Meshify][MeshifyReceiver]";

    private Config config;

    private Context context;

    private boolean isRegistered = false;

    private BluetoothController bluetoothController;

    protected MeshifyReceiver(Config config, Context context) throws MeshifyException {
        this.config = config;
        this.context = context;
        createBluetoothController(context, config);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        onReceiveAction(context, intent);
    }

    private void onReceiveAction(Context context, Intent intent) {
        switch (this.config.getAntennaType()) {
            case BLUETOOTH:
            case BLUETOOTH_LE: {
                this.bluetoothController.onReceiveAction(intent, context);
            }
        }
    }

    private IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        switch (this.config.getAntennaType()) {
            case BLUETOOTH: {
                this.addBluetoothActions(intentFilter);
                this.addBleAction(intentFilter);
            }
            case BLUETOOTH_LE: {
                this.addBleAction(intentFilter);
            }
        }
        return intentFilter;
    }

    private void addBleAction(IntentFilter intentFilter) {
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
    }

    private void addBluetoothActions(IntentFilter intentFilter) {
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothDevice.ACTION_UUID);
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                intentFilter.addAction((String) BluetoothDevice.class.getDeclaredField("ACTION_SDP_RECORD").get((Object) null));
                intentFilter.addAction((String) BluetoothDevice.class.getDeclaredField("EXTRA_SDP_SEARCH_STATUS").get((Object) null));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void registerReceiver(Context context) {
        if (!this.isRegistered) {
            context.registerReceiver(this, getIntentFilter());
            this.isRegistered = true;
        }
    }

    public void unregisterReceiver(Context context) {
        if (this.isRegistered) {
            context.unregisterReceiver(this);
            this.isRegistered = false;
        }
    }

    private void createBluetoothController(Context context, Config config) throws MeshifyException {
        switch (this.config.getAntennaType()) {
            case BLUETOOTH:
            case BLUETOOTH_LE: {
                this.bluetoothController = new BluetoothController(context, config);
            }
        }

    }

    public void startServer(Config.Antenna antenna) {
        Log.d(TAG, "startServer: " + antenna);
        switch (antenna) {
            case BLUETOOTH:
            case BLUETOOTH_LE: {
                this.startServer();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void startServer() {
        if (MeshifyUtils.getBluetoothAdapter(this.context).isEnabled()) {
            try {
                this.bluetoothController.startServer(this.context);
            } catch (ConnectionException e) {

            }
        }
    }

    public void onBluetoothServerStop(Config.Antenna antenna) {
        switch (antenna) {
            case BLUETOOTH:
            case BLUETOOTH_LE: {
                this.onBluetoothServerStop();
            }
        }
    }

    public void startDiscovery(Config.Antenna antenna) {
        switch (antenna) {
            case BLUETOOTH:
            case BLUETOOTH_LE: {
                this.bluetoothController.startDiscovery(this.context);
            }
        }
    }

    private void onBluetoothServerStop() {

    }



}
