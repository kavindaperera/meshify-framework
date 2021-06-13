package com.codewizards.meshify.framework.controllers;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import com.codewizards.meshify.client.Config;
import com.codewizards.meshify.client.Device;
import com.codewizards.meshify.client.MeshifyUtils;
import com.codewizards.meshify.framework.expections.ConnectionException;
import com.codewizards.meshify.logs.Log;
import com.google.gson.GsonBuilder;

import java.io.IOException;

public class BluetoothServer extends ThreadServer<BluetoothSocket, BluetoothServerSocket>  {

    final String TAG = "[Meshify][BluetoothServer]";

    @SuppressLint("MissingPermission")
    public BluetoothServer(Config config, boolean isSecure, Context context) throws ConnectionException {
        super(config, context);
        BluetoothAdapter bluetoothAdapter = MeshifyUtils.getBluetoothAdapter(context);

        try {
            Log.d(TAG, "BluetoothServer: uuid = " + BluetoothUtils.getBluetoothUuid());
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
        Log.i(TAG, "startServer: is connected: " + this.alive());
        if (!this.alive()){
            this.setRunning(true);
            super.start();
        }
    }

    @Override
    public void stopServer() throws ConnectionException {
        if (this.alive()) {
            try {
                this.setRunning(false);
                ((BluetoothServerSocket)this.getServerSocket()).close();
            }
            catch (IOException iOException) {
                throw new ConnectionException(iOException);
            }
            finally {
                super.interrupt();
            }
        }
    }

    void acceptConnection(BluetoothSocket bluetoothSocket) {
        Log.d(TAG, "acceptConnection: device " + bluetoothSocket.getRemoteDevice().getAddress());
    }

    @Override
    boolean alive() {
        return super.isAlive() && this.getServerSocket() != null;
    }

    @Override
    public void run() {

        while (this.isRunning()){
            Log.d(TAG, "runServer: is alive: " + this.alive());
            if (this.getServerSocket() == null) {
                Log.e(TAG, "run: null server_socket, stopServer");
                try {
                    this.stopServer();
                }
                catch (ConnectionException connectionException) {
                    Log.e(TAG, "run: stopServer exception", connectionException);
                }
            }
            try {
                this.acceptConnection(((BluetoothServerSocket)this.getServerSocket()).accept()); //waiting to accept
            }
            catch (IOException e) {
                Log.e(TAG, "runServer:IOException" + e.getMessage());
            }
        }

    }
}
