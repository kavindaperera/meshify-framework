package com.codewizards.meshify.framework.controllers.bluetoothLe.gatt;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.Context;
import android.os.Build;

public class BleGatt {

    private final Context context;

    BleGatt(Context context) {
        this.context = context;
    }

    public BluetoothGatt connectGatt(BluetoothDevice bluetoothDevice, boolean autoConnect, BluetoothGattCallback callback) {
        if (bluetoothDevice == null) {
            return null;
        }

        if (Build.VERSION.SDK_INT >= 24 || !autoConnect) {
            return this.connectGatt(callback, bluetoothDevice, false);
        }

        // TODO
        return null;
    }

    private BluetoothGatt connectGatt(BluetoothGattCallback bluetoothGattCallback, BluetoothDevice bluetoothDevice, boolean autoConnect) {
        if (Build.VERSION.SDK_INT >= 23) {
            return bluetoothDevice.connectGatt(this.context, autoConnect, bluetoothGattCallback, 0);
        }
        return bluetoothDevice.connectGatt(this.context, autoConnect, bluetoothGattCallback);
    }

}
