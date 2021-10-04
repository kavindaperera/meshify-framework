package com.codewizards.meshify.framework.controllers;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;

import com.codewizards.meshify.client.Config;
import com.codewizards.meshify.framework.expections.ConnectionException;

class BluetoothLeServer  extends ThreadServer<BluetoothDevice, BluetoothGattServer> {

    final String TAG = "[Meshify][BleServer]";

    private BluetoothManager bluetoothManager;

    private BluetoothGattService bluetoothGattService;

    private boolean isRunningLe = false;

    Object object = new Object();

    protected BluetoothLeServer(Config config, Context context) throws ConnectionException {
        super(config, context);
        this.bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        this.openGattServer(context);
        Log.d(TAG, "BluetoothLeServer: bluetooth le server created.");
    }



    @Override
    public void startServer() throws ConnectionException {
        //TODO -> [kavinda]
        Object object = this.object;
        synchronized (object) {
            if (!this.isAlive() && !this.isRunningLe()) {
                this.setRunning(true);
                this.setRunningLe(true);
                this.run();
            }
            else {
                Log.e(TAG, "startServer: trying started server fail, server was started.");
            }
        }
    }

    @Override
    public void stopServer() throws ConnectionException {
        //TODO -> [kavinda]
    }



    @Override
    boolean alive() {
        return this.getServerSocket() != null && ((BluetoothGattServer) this.getServerSocket()).getServices().size() > 0;
    }

    public boolean isRunningLe() {
        return this.isRunningLe;
    }

    public void setRunningLe(boolean b) {
        this.isRunningLe = b;
    }

    @Override
    public void run() {
        super.run();

        if (this.getServerSocket() != null) {
            this.bluetoothGattService = ((BluetoothGattServer) this.getServerSocket()).getService(BluetoothUtils.getBluetoothUuid());
        }

        if (this.bluetoothGattService == null && this.getServerSocket() != null) {
            this.bluetoothGattService = new BluetoothGattService(BluetoothUtils.getBluetoothUuid(), 0);

            //TODO - addService

        }

    }



    private void openGattServer(Context context) {
        if (this.getServerSocket() == null) {
            this.acceptConnection(this.bluetoothManager.openGattServer(context, new GattServerCallback()));
        }
    }
}
