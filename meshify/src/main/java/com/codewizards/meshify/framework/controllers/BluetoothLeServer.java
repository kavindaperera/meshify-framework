package com.codewizards.meshify.framework.controllers;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;

import com.codewizards.meshify.client.Config;
import com.codewizards.meshify.framework.expections.ConnectionException;

class BluetoothLeServer  extends ThreadServer<BluetoothDevice, BluetoothGattServer> {

    final String TAG = "[Meshify][BleServer]";

    private BluetoothManager bluetoothManager;

    private BluetoothGattServer bluetoothGattServer;

    protected BluetoothLeServer(Config config, Context context) throws ConnectionException {
        super(config, context);
        this.bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        Log.d(TAG, "BluetoothLeServer: bluetooth le server created.");
    }

    @Override
    public void startServer() throws ConnectionException {
        //TODO
    }

    @Override
    public void stopServer() throws ConnectionException {
        //TODO
    }

    @Override
    boolean alive() {
        return this.getServerSocket() != null && ((BluetoothGattServer) this.getServerSocket()).getServices().size() > 0;
    }

    @Override
    public void run() {
        super.run();
    }
}
