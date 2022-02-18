package com.codewizards.meshify.framework.controllers.bluetoothLe.gatt;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Parcel;

import com.codewizards.meshify.api.Config;
import com.codewizards.meshify.api.Device;
import com.codewizards.meshify.framework.controllers.bluetoothLe.BleMeshifyDevice;
import com.codewizards.meshify.framework.controllers.bluetoothLe.BluetoothLeServer;
import com.codewizards.meshify.framework.controllers.connection.ConnectionManager;
import com.codewizards.meshify.framework.controllers.discoverymanager.DeviceManager;
import com.codewizards.meshify.framework.controllers.discoverymanager.ServerFactory;
import com.codewizards.meshify.framework.controllers.helper.MeshifyUtils;
import com.codewizards.meshify.framework.controllers.sessionmanager.Session;
import com.codewizards.meshify.framework.controllers.sessionmanager.SessionManager;
import com.codewizards.meshify.framework.entities.MeshifyEntity;
import com.codewizards.meshify.logs.Log;

public class GattServerCallback extends BluetoothGattServerCallback {

    final String TAG = "[Meshify][GattServerCallback]";

    public GattServerCallback() {
    }

    @Override
    public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
        super.onConnectionStateChange(device, status, newState);
        Log.e(TAG,"onConnectionStateChange()" + " | newState: " + newState + " | status: " + status);

        switch (status) {
            case BluetoothGatt.GATT_CONNECTION_CONGESTED: {
                Log.e(TAG, "Remote device connection is congested" + status);
                break;
            }
            case BluetoothGatt.GATT_FAILURE: {
                Log.e(TAG, "GATT operation failed" + status);
                break;
            }
        }

        switch (newState) {
            case BluetoothProfile.STATE_DISCONNECTED: {

                Session session = SessionManager.getSession(device.getAddress());

                if (session == null || session.getState() == 1 || ConnectionManager.getMeshifyDevice() != null && ConnectionManager.getMeshifyDevice().getDevice().getDeviceAddress().equals(device.getAddress())) break;

                Log.e(TAG, "STATE_DISCONNECTED " + device.getAddress());

                if (this.getBluetoothLeServer() != null && this.getBluetoothLeServer().getServerSocket() != null) {
                    ((BluetoothGattServer)this.getBluetoothLeServer().getServerSocket()).cancelConnection(device);
                }
                session.removeSession();
            }
            case BluetoothProfile.STATE_CONNECTED: {
                Log.e(TAG, "Client connecting to Server : STATE_CONNECTED " + device.getAddress());
                if (SessionManager.sessionMap.get(device.getAddress()) == null && this.getBluetoothLeServer() != null && this.getBluetoothLeServer().getServerSocket() != null && this.getBluetoothLeServer().isAlive()) {
                    Log.e(TAG, "Server Connection with " + device.getAddress());
                }
            }
        }

    }

    @Override
    public void onServiceAdded(int status, BluetoothGattService service) {
        super.onServiceAdded(status, service);
        Log.e(TAG,"onServiceAdded()" + " | service: " + service.getUuid().toString() + " | status: " + status);
    }

    @Override
    public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
        Log.e(TAG,"onCharacteristicReadRequest()" + " | device: " + device + " | requestId: " + requestId + " | offset: "  + offset + " | characteristic: " + characteristic);
    }

    @Override
    public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
        super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
        Log.e(TAG,"onCharacteristicWriteRequest()" + " | device: " + device + " | requestId: " + requestId + " | offset: "  + offset + " | characteristic: " + characteristic);
        (this.getBluetoothLeServer().getServerSocket()).sendResponse(device, requestId, 0, offset, value);
        BluetoothDevice bluetoothDevice = device;
        Session session = SessionManager.getSession(device.getAddress());
        if (session != null && session.getBluetoothDevice() != null && session.getBluetoothDevice().equals(bluetoothDevice)) {
            Parcel parcel = MeshifyUtils.unmarshall(value);
            MeshifyEntity meshifyEntity = MeshifyEntity.CREATOR.createFromParcel(parcel);
            Log.e(TAG, "Received: " + meshifyEntity);
            session.processEntity(meshifyEntity);
        }
    }

    @Override
    public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
        super.onDescriptorReadRequest(device, requestId, offset, descriptor);
        Log.e(TAG,"onDescriptorReadRequest()" + " | device: " + device + " | requestId: " + requestId + " | offset: "  + offset + " | descriptor: " + descriptor);
    }

    @Override
    public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
        super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
        Log.e(TAG,"onDescriptorWriteRequest()" + " | device: " + device + " | requestId: " + requestId + " | descriptor: " + descriptor.getValue());
        (this.getBluetoothLeServer().getServerSocket()).sendResponse(device, requestId, 0, offset, value);
    }

    @Override
    public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
        super.onExecuteWrite(device, requestId, execute);
        Log.e(TAG,"onExecuteWrite()" + " | device: " + device + " | requestId: " + requestId + " | execute: " + execute);
    }

    @Override
    public void onNotificationSent(BluetoothDevice device, int status) {
        super.onNotificationSent(device, status);
        Log.e(TAG,"onNotificationSent()" + " | device: " + device + " | status: " + status);
    }

    @Override
    public void onMtuChanged(BluetoothDevice device, int mtu) {
        super.onMtuChanged(device, mtu);
        Log.e(TAG,"onMtuChanged()" + " | mtu: " + mtu + " | device: " + device);
    }

    @Override
    public void onPhyUpdate(BluetoothDevice device, int txPhy, int rxPhy, int status) {
        super.onPhyUpdate(device, txPhy, rxPhy, status);
        Log.e(TAG, "onPhyUpdate(): txPhy: " + txPhy + " | rxPhy: " + rxPhy + " | status: " + status);
    }

    @Override
    public void onPhyRead(BluetoothDevice device, int txPhy, int rxPhy, int status) {
        super.onPhyRead(device, txPhy, rxPhy, status);
        Log.e(TAG, "onPhyRead(): txPhy: " + txPhy + " | rxPhy: " + rxPhy + " | status: " + status);
    }

    private BluetoothLeServer getBluetoothLeServer() {
        return (BluetoothLeServer)ServerFactory.getServerInstance(Config.Antenna.BLUETOOTH_LE, true);
    }



}
