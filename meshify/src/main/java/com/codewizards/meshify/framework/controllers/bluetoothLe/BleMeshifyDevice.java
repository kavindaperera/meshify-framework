package com.codewizards.meshify.framework.controllers.bluetoothLe;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import com.codewizards.meshify.client.Device;
import com.codewizards.meshify.client.Meshify;
import com.codewizards.meshify.framework.controllers.Session;
import com.codewizards.meshify.framework.controllers.SessionManager;
import com.codewizards.meshify.framework.controllers.base.MeshifyDevice;
import com.codewizards.meshify.framework.controllers.bluetoothLe.gatt.BluetoothLeGatt;
import com.codewizards.meshify.logs.Log;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;

public class BleMeshifyDevice extends MeshifyDevice {

    final String TAG = "[Meshify][BleMeshifyDevice]";

    CompletableEmitter completableEmitter;

    GattCallback gattCallback = new GattCallback();


    public BleMeshifyDevice(Device device) {
        super(device);
    }

    @Override
    public Completable create() {

        return Completable.create(CompletableEmitter -> {
            this.completableEmitter = completableEmitter;

            BluetoothDevice bluetoothDevice = this.getDevice().getBluetoothDevice();

            if (Meshify.getInstance().getMeshifyCore() != null) {

                Log.i(TAG, "Connecting GATT " + this.getDevice().getBluetoothDevice().getAddress() + " " + this.getDevice().getUserId());

                BluetoothLeGatt bluetoothLeGatt = new BluetoothLeGatt(Meshify.getInstance().getMeshifyCore().getContext());

                bluetoothLeGatt.connectGatt(bluetoothDevice, false, this.gattCallback);

                // TODO - connect as client - create session

                Log.e(this.TAG, "connect as client device address: " + this.getDevice().getDeviceAddress());

                Session session = SessionManager.getSession(this.getDevice().getDeviceAddress());
                if (session == null) {
                    session = new Session(bluetoothDevice, true, this.completableEmitter);
                }

            }
        });
    }

    /**
     * The main interface that the app has to implement in order to receive callbacks for most BluetoothGatt-related operations
     * like reading, writing, or getting notified about incoming notifications or indications.
     */
    private class GattCallback extends BluetoothGattCallback {

        public GattCallback() {
        }

        @Override
        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status);
            Log.e(BleMeshifyDevice.this.TAG, "onPhyUpdate(): txPhy: " + txPhy + " | rxPhy: " + rxPhy + " | status: " + status);
        }

        @Override
        public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyRead(gatt, txPhy, rxPhy, status);
            Log.e(BleMeshifyDevice.this.TAG, "onPhyRead(): txPhy: " + txPhy + " | rxPhy: " + rxPhy + " | status: " + status);
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.e(BleMeshifyDevice.this.TAG,"onConnectionStateChange()" + " | newState: " + newState + " | status: " + status);

        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.e(BleMeshifyDevice.this.TAG,"onMtuChanged()");

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.e(BleMeshifyDevice.this.TAG,"onServicesDiscovered()");
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.e(BleMeshifyDevice.this.TAG,"onCharacteristicRead()");

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.e(BleMeshifyDevice.this.TAG,"onCharacteristicWrite()");

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.e(BleMeshifyDevice.this.TAG,"onCharacteristicChanged()");

        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.e(BleMeshifyDevice.this.TAG,"onDescriptorRead()");

        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.e(BleMeshifyDevice.this.TAG,"onDescriptorWrite()");

        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
            Log.e(BleMeshifyDevice.this.TAG,"onReliableWriteCompleted()");

        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            Log.e(BleMeshifyDevice.this.TAG,"onReadRemoteRssi()");

        }
    }

}
