package com.codewizards.meshify.framework.controllers;

import android.content.Context;

import com.codewizards.meshify.client.Config;
import com.codewizards.meshify.client.Meshify;
import com.codewizards.meshify.framework.expections.ConnectionException;
import com.codewizards.meshify.logs.Log;

public class ServerFactory {

    private static final String TAG = "[Meshify][ServerFactory]";

    private static BluetoothServer bluetoothServer;


    static ThreadServer getServerInstance(Config.Antenna antenna, boolean isNew, Context context) {

        switch (antenna) {
            case BLUETOOTH: {
                if (bluetoothServer == null && isNew) {
                    try {
                        Log.d(TAG, "getServerInstance: new bluetooth server created");
                        bluetoothServer = new BluetoothServer(Meshify.getInstance().getConfig(), false, context);
                    } catch (ConnectionException e) {
                        Log.e(TAG, "getServerInstance: Error occurred while initiating BluetoothServer", e);
                    }
                }
                return bluetoothServer;
            }

            case BLUETOOTH_LE:{
                return null;
            }

        }
        throw new IllegalArgumentException("Invalid server type found");

    }


}
