package com.codewizards.meshify.framework.controllers.discoverymanager;

import com.codewizards.meshify.api.Config;
import com.codewizards.meshify.api.Meshify;
import com.codewizards.meshify.framework.controllers.bluetooth.BluetoothServer;
import com.codewizards.meshify.framework.controllers.bluetoothLe.BluetoothLeServer;
import com.codewizards.meshify.framework.expections.ConnectionException;
import com.codewizards.meshify.logs.Log;

public class ServerFactory {

    private static final String TAG = "[Meshify][ServerFactory]";

    private static BluetoothServer bluetoothServer;

    private static BluetoothLeServer bluetoothLeServer;


    public static ThreadServer getServerInstance(Config.Antenna antenna, boolean isNew) {

        switch (antenna) {
            case BLUETOOTH: {
                if (bluetoothServer == null && isNew) {
                    try {
                        Log.d(TAG, "getServerInstance: new bluetooth server created");
                        bluetoothServer = new BluetoothServer(Meshify.getInstance().getConfig(), false, Meshify.getInstance().getMeshifyCore().getContext());
                    } catch (ConnectionException e) {
                        Log.e(TAG, "getServerInstance: Error occurred while initiating BluetoothServer", e);
                    }
                }
                return bluetoothServer;
            }

            case BLUETOOTH_LE:{
                if (bluetoothLeServer == null && isNew) {
                    try {
                        Log.d(TAG, "getServerInstance: new bluetooth le server created");
                        bluetoothLeServer = new BluetoothLeServer(Meshify.getInstance().getConfig(), Meshify.getInstance().getMeshifyCore().getContext());
                    } catch (ConnectionException exception) {
                        Log.e(TAG, "getServerInstance: Error occurred while initiating BluetoothServer", exception);
                    }
                }
                return bluetoothLeServer;
            }
        }
        throw new IllegalArgumentException("Invalid server type found");
    }

    static void setBluetoothServer(BluetoothServer bluetoothServer) {
        ServerFactory.bluetoothServer = bluetoothServer;
    }

    static void setBluetoothLeServer(BluetoothLeServer bluetoothLeServer) {
        ServerFactory.bluetoothLeServer = bluetoothLeServer;
    }

}
