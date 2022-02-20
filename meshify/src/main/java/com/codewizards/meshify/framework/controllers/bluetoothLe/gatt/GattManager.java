package com.codewizards.meshify.framework.controllers.bluetoothLe.gatt;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;

import com.codewizards.meshify.logs.Log;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GattManager {

    final String TAG = "[Meshify][GattManager]";

    private ConcurrentLinkedQueue<GattOperation> gattOperations = new ConcurrentLinkedQueue<>();

    private ConcurrentHashMap<String, BluetoothGatt> bluetoothGattMap = new ConcurrentHashMap();

    private GattOperation gatt = null;

    public GattManager() {
    }

    public synchronized void addGattOperation(GattOperation gattOperation1) {
        Log.e(TAG, "addGattOperation(): " + gattOperation1);
        this.gattOperations.add(gattOperation1);
        this.start();
    }


    public synchronized void start(){
        Log.e(TAG, "start(): gatt_operations | size: " + this.gattOperations.size());
        if (this.gatt == null && this.gattOperations.size() > 0) {
            GattOperation poll = this.gattOperations.poll();
            this.start(poll);
            BluetoothDevice bluetoothDevice = poll.getBluetoothDevice();
            if (this.bluetoothGattMap.containsKey(bluetoothDevice.getAddress())) {
                Log.e(TAG, "writeDescriptor(): ");
                execute(this.bluetoothGattMap.get(bluetoothDevice.getAddress()), poll);
            }
        }
    }

    public synchronized void start(GattOperation gattOperation) {
        Log.e(TAG, "start(): " + gattOperation);
        this.gatt = gattOperation;
        if (gattOperation == null) {
            this.start();
        }
    }

    public void execute(BluetoothGatt bluetoothGatt, GattOperation gatt) {
        if (gatt == this.gatt) {
            gatt.writeDescriptor(bluetoothGatt);
            if (!gatt.isGatt()) {
                this.start((GattOperation) null);
                this.start();
            }
        }
    }

    public void removeGattOperation(BluetoothDevice bluetoothDevice) {
        for (GattOperation gattOperation2 : this.gattOperations) {
            if (!gatt.getBluetoothDevice().equals((Object)bluetoothDevice)) continue;
            if (this.getGattOperation() != null && this.getGattOperation().equals(gattOperation2)) {
                //
            }
            this.gattOperations.remove(gattOperation2);
        }
    }

    public GattOperation getGattOperation() {
        return this.gatt;
    }

    public ConcurrentHashMap<String, BluetoothGatt> getBluetoothGattMap() {
        return this.bluetoothGattMap;
    }

    public void addAndStart(GattTransaction gattTransaction) {
        for (GattOperation gattOperation : gattTransaction.getGattOperations()) {
            this.gattOperations.add(gattOperation);
        }
        this.start();
    }

}
