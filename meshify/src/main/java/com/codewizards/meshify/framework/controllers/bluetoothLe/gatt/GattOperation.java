package com.codewizards.meshify.framework.controllers.bluetoothLe.gatt;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;

import java.util.UUID;

public abstract class GattOperation {

    private BluetoothDevice bluetoothDevice;

    private GattTransaction gattTransaction;

    private String operationId = UUID.randomUUID().toString();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GattOperation that = (GattOperation) o;
        return this.operationId.equals(that.operationId);
    }

    @Override
    public int hashCode() {
        return this.operationId.hashCode();
    }

    @Override
    public String toString() {
        return "GattOperation{" +
                ", operationId='" + operationId + '\'' +
                '}';
    }

    public GattOperation(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public GattTransaction getGattTransaction() {
        return gattTransaction;
    }

    public void setGattTransaction(GattTransaction gattTransaction1) {
        this.gattTransaction = gattTransaction1;
    }

    public String getOperationId() {
        return this.operationId;
    }

    int getWaitTime() {
        return 20000;
    }

    public abstract void writeDescriptor(BluetoothGatt var1);

}
