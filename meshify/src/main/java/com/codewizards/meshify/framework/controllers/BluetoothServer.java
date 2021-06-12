package com.codewizards.meshify.framework.controllers;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import com.codewizards.meshify.client.Config;
import com.codewizards.meshify.client.MeshifyUtils;
import com.codewizards.meshify.framework.expections.ConnectionException;
import com.codewizards.meshify.logs.Log;

import java.io.IOException;

public class BluetoothServer extends ThreadServer<BluetoothSocket, BluetoothServerSocket>  {

    final String TAG = "[Meshify][BluetoothServer]";

    @SuppressLint("MissingPermission")
    public BluetoothServer(Config config, boolean isSecure, Context context) throws ConnectionException {
        super(config, context);
        BluetoothAdapter bluetoothAdapter = MeshifyUtils.getBluetoothAdapter(context);

        try {
            Log.d(TAG, "BluetoothServer: " + BluetoothUtils.getBluetoothUuid());
            if (isSecure) {
                this.acceptConnection(bluetoothAdapter.listenUsingRfcommWithServiceRecord(bluetoothAdapter.getName(), BluetoothUtils.getBluetoothUuid()));
            } else {
                this.acceptConnection(bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(bluetoothAdapter.getName(), BluetoothUtils.getBluetoothUuid()));
            }
        } catch (IOException e) {
            throw new ConnectionException(e.getMessage(), e);
        }

    }


    @Override
    public void startServer() throws ConnectionException {

    }

    @Override
    public void stopServer() throws ConnectionException {

    }

    @Override
    boolean alive() {
        return false;
    }
}
