package com.codewizards.meshify.framework.controllers;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;

import com.codewizards.meshify.client.Device;
import com.codewizards.meshify.client.Meshify;
import com.codewizards.meshify.client.MeshifyUtils;
import com.codewizards.meshify.logs.Log;

import java.io.IOException;

import io.reactivex.Completable;
import io.reactivex.functions.Function;

/**
 * <p>This class represents a Bluetooth-enabled meshify device..</p>
 *
 * @author Kavinda Perera
 * @version 1.0
 */
public class BluetoothMeshifyDevice extends MeshifyDevice {

    final String TAG = "[Meshify][BluetoothMeshifyDevice]";

    private boolean isSecure = false;

    public BluetoothMeshifyDevice(Device device, boolean isSecure) {
        super(device);
        this.isSecure = isSecure;
    }

    /**
     * @return a RxJava Completable object
     */
    @SuppressLint("MissingPermission")
    @Override
    public Completable create() {
        return Completable.create(completableEmitter -> {
            try {
                BluetoothAdapter bluetoothAdapter = MeshifyUtils.getBluetoothAdapter(Meshify.getInstance().getMeshifyCore().getContext());
                if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.cancelDiscovery();
                }
                BluetoothSocket bluetoothSocket = this.isSecure ? this.getDevice().getBluetoothDevice().createRfcommSocketToServiceRecord(BluetoothUtils.getBluetoothUuid()) : this.getDevice().getBluetoothDevice().createInsecureRfcommSocketToServiceRecord(BluetoothUtils.getBluetoothUuid());
                bluetoothSocket.connect();
                Log.d(TAG, "connected: " + BluetoothUtils.getBluetoothUuid());
                Session session = new Session(bluetoothSocket);
                session.setSessionId(this.getDevice().getDeviceAddress());
                session.setClient(true);
                this.getDevice().setSessionId(session.getSessionId());
                session.setDevice(this.getDevice());
                SessionManager.queueSession(session);
                DeviceManager.addDevice(session.getDevice(), session); //TODO - Remove session
                completableEmitter.onComplete(); //call connection subscriber onComplete
            }
            catch (IOException iOException) {
                Log.e(TAG, "connect: fail [ " + iOException.getMessage() + " ]");
                completableEmitter.tryOnError((Throwable)iOException); //call connection subscriber onError
            }
        }).retryWhen(new RetryWhenLambda(4, 2000));
    }
}
