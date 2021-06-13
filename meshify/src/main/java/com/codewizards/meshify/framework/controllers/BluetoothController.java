package com.codewizards.meshify.framework.controllers;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;

import com.codewizards.meshify.client.Config;
import com.codewizards.meshify.client.Meshify;
import com.codewizards.meshify.client.MeshifyException;
import com.codewizards.meshify.client.MeshifyUtils;
import com.codewizards.meshify.framework.expections.ConnectionException;
import com.codewizards.meshify.logs.Log;

public class BluetoothController {

    private static String TAG = "[Meshify][BluetoothController]";

    static int state = 0;

    private Config config;

    private Context context;

    private BluetoothDiscovery bluetoothDiscovery;

    private ThreadServer threadServer;

    private boolean isBLE = true;

    private BluetoothAdapter bluetoothAdapter;


    public BluetoothController(Context context, Config config) throws MeshifyException {
        this.context = context;
        this.config = config;
        switch (this.getConfig().getAntennaType()){
            case BLUETOOTH_LE: {
                this.hardwareCheck();
            }
            case BLUETOOTH: {
                this.requestDiscoverable();
            }
        }
    }

    private void hardwareCheck() throws MeshifyException {
        Log.d(TAG, "hardwareCheck:");
        if (!getContext().getPackageManager().hasSystemFeature("android.hardware.bluetooth_le")) {
            this.isBLE = false;

            switch (this.getConfig().getAntennaType()){
                case BLUETOOTH_LE: {
                    Log.e(TAG, "Bluetooth Low Energy not supported.");
                    getConfig().setAntennaType(Config.Antenna.UNREACHABLE);
                    throw new MeshifyException(0, "Bluetooth Low Energy not supported.");
                }
                case BLUETOOTH: {
                    getConfig().setAntennaType(Config.Antenna.BLUETOOTH);
                }
            }
        } else {
            //BLE
        }
    }

    @SuppressLint("MissingPermission")
    private void requestDiscoverable() {
        Log.d(TAG, "requestDiscoverable:");
        BluetoothAdapter bluetoothAdapter = MeshifyUtils.getBluetoothAdapter(getContext());
        this.bluetoothAdapter = bluetoothAdapter;
        if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE && Meshify.getInstance().getConfig().getAntennaType() == Config.Antenna.BLUETOOTH) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 600);
            getContext().startActivity(intent);
        }
    }

    public void startDiscovery(Context context) {
        Log.d(TAG, "startDiscovery:");
        switch (this.getConfig().getAntennaType()) {
            case BLUETOOTH: {
                this.startBluetoothDiscovery(context);
                break;
            }
            case BLUETOOTH_LE: {
                //TODO - startBluetoothLeDiscovery
            }
        }
    }

    private void startBluetoothDiscovery(Context context) {
        Log.d(TAG, "startBluetoothDiscovery: " );

        if (this.bluetoothDiscovery == null) {
            this.bluetoothDiscovery = new BluetoothDiscovery(context);
        }

        if (!this.bluetoothDiscovery.isDiscoveryRunning()) {
            this.bluetoothDiscovery.startDiscovery(context, getConfig());
        }
    }

    public void startServer(Context context) throws ConnectionException {
        Log.d(TAG, "startServer: " + this.getConfig().getAntennaType());
        switch (this.getConfig().getAntennaType()) {
            case BLUETOOTH: {
                startBluetoothServer(context);
                break;
            }
            case BLUETOOTH_LE: {
                //TODO - start BLE server
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void startBluetoothServer(Context context) throws ConnectionException {
        Log.d(TAG, "startBluetoothServer:" );
        ThreadServer threadServer = ServerFactory.getServerInstance(Config.Antenna.BLUETOOTH, true, context.getApplicationContext());
        this.threadServer = threadServer;
        if (threadServer != null) {
            threadServer.startServer();
        }
        if (this.bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) { //get discoverable permissions
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0); //value 0 means 120 seconds discoverable duration
            getContext().startActivity(intent);
        }
    }

    public Config getConfig() {
        return this.config;
    }

    public Context getContext() {
        return this.context;
    }


}
