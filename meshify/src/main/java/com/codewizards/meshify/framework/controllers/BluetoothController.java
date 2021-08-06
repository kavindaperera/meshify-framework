package com.codewizards.meshify.framework.controllers;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

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

    private ThreadServer threadServerBle;

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
        BluetoothAdapter bluetoothAdapter = MeshifyUtils.getBluetoothAdapter(getContext());
        this.bluetoothAdapter = bluetoothAdapter;
        if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE && Meshify.getInstance().getConfig().getAntennaType() == Config.Antenna.BLUETOOTH) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
            getContext().startActivity(intent);
        }
    }

    private void disconnectDevices() {
        switch (this.getConfig().getAntennaType()) {
            case BLUETOOTH: {
                //TODO - remove BT sessions
                break;
            }
            case BLUETOOTH_LE: {
                //TODO - remove BLE sessions
            }
        }
    }

    private void stateChangeAction(Intent intent, Context context) {
        BluetoothController bluetooth_controller = this;
        synchronized (bluetooth_controller) {
            switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0)) {
                case BluetoothAdapter.STATE_ON: {
                    try {
                        this.startServer(context.getApplicationContext());
                    } catch (ConnectionException e) {
                        e.printStackTrace();
                    }
                }
                case BluetoothAdapter.STATE_OFF: {
                    try {
                        stopDiscovery(context.getApplicationContext());
                        disconnectDevices();
                        stopServer(context.getApplicationContext());
                    } catch (Exception e2) {
                        Log.w(TAG, e2.getMessage());
                    }
                }

            }
        }
    }

    @SuppressLint("MissingPermission")
    private void discover(Context context) {
        Log.d(TAG, "discovering... ");
        if (this.bluetoothDiscovery != null) {
            this.bluetoothDiscovery.setDiscoveryRunning(true);
            return;
        } else {
            this.bluetoothAdapter.cancelDiscovery();
            BluetoothDiscovery bluetoothDiscovery =  new BluetoothDiscovery(context);
            this.bluetoothDiscovery = bluetoothDiscovery;

            this.bluetoothDiscovery.stopDiscovery(context);
            this.bluetoothDiscovery.startDiscovery(context, getConfig());
        }

    }


    public void startDiscovery(Context context) {
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
        if (this.bluetoothDiscovery == null) {
            this.bluetoothDiscovery = new BluetoothDiscovery(context);
        }

        if (!this.bluetoothDiscovery.isDiscoveryRunning()) {
            this.bluetoothDiscovery.startDiscovery(context, getConfig());
        }
    }

    public void stopDiscovery(Context context) {
        if (this.bluetoothDiscovery != null) {
            this.bluetoothDiscovery.stopDiscovery(context);
        }

        //TODO - stop ble discovery

    }

    public void startServer(Context context) throws ConnectionException {
        Log.d(TAG, "startServer: " + this.getConfig().getAntennaType());
        switch (this.getConfig().getAntennaType()) {
            case BLUETOOTH: {
                startBluetoothServer(context);
                break;
            }
            case BLUETOOTH_LE: {
                startBluetoothLeServer(context);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void startBluetoothServer(Context context) throws ConnectionException {
        Log.d(TAG, "startBluetoothServer:" );
        ThreadServer threadServer = ServerFactory.getServerInstance(Config.Antenna.BLUETOOTH, true);
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

    private void startBluetoothLeServer(Context context) {
        if (this.isBLE && this.bluetoothAdapter.getBluetoothLeAdvertiser() != null) {
            this.threadServerBle = ServerFactory.getServerInstance(Config.Antenna.BLUETOOTH_LE, true);
            try {
                this.threadServerBle.startServer();
            }
            catch (ConnectionException connectionException) {
                Log.e(TAG, "startBluetoothLeServer:", connectionException);
            }
        } else {
            //empty
        }
    }

    public void stopServer(Context context) throws ConnectionException {
        Log.i(TAG, "stopServer: ");
        switch (this.getConfig().getAntennaType()) {
            case BLUETOOTH: {
                this.stopBluetoothServer(context.getApplicationContext());
                break;
            }
            case BLUETOOTH_LE: {
                //TODO - stop BLE server
            }
        }
    }

    private void stopBluetoothServer(Context context) throws ConnectionException {
        ThreadServer threadServer = ServerFactory.getServerInstance(Config.Antenna.BLUETOOTH_LE, false);
        this.threadServer = threadServer;
        if (threadServer != null) {
            threadServer.stopServer();
            this.threadServer = null;
            ServerFactory.setBluetoothServer((BluetoothServer) null);
        }
    }

    @SuppressLint("MissingPermission")
    public void onReceiveAction(Intent intent, Context context) {
        String actionLog = intent.getAction();
        Log.d(TAG, "onReceiveAction: " + actionLog);

        String action = intent.getAction();
        switch (intent.getAction()) {
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                this.stateChangeAction(intent, context);
                break;
            case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                this.discover(context);
                break;
            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                this.bluetoothDiscovery.DiscoveryFinishedAction(context);
                break;
            case BluetoothDevice.ACTION_FOUND:
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                this.bluetoothDiscovery.addBluetoothDevice(intent);
                break;
            case BluetoothDevice.ACTION_UUID:
                this.bluetoothDiscovery.pair(intent);
        }

    }

    public Config getConfig() {
        return this.config;
    }

    public Context getContext() {
        return this.context;
    }


}
