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


    synchronized void start(){
        if (this.gatt == null && this.gattOperations.size() > 0) {
            GattOperation poll = this.gattOperations.poll();
            this.start(poll);
            BluetoothDevice a = poll.getBluetoothDevice();
            if (this.bluetoothGattMap.containsKey(a.getAddress())) {
                writeDescriptor(this.bluetoothGattMap.get(a.getAddress()), poll);
            }
        }
    }

    public synchronized void start(GattOperation gattOperation) {
        this.gatt = gattOperation;
        if (gattOperation == null) {
            this.start();
        }
    }

    public void writeDescriptor(BluetoothGatt bluetoothGatt, GattOperation gatt) {
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
            if (this.getGatt() != null && this.getGatt().equals(gattOperation2)) {
                //
            }
            this.gattOperations.remove(gattOperation2);
        }
    }

    public GattOperation getGatt() {
        return this.gatt;
    }

    public ConcurrentHashMap<String, BluetoothGatt> getBluetoothGattMap() {
        return this.bluetoothGattMap;
    }

}
