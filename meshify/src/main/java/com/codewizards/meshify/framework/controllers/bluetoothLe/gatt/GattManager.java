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

    private GattOperation gattOperation = null;

    public GattManager() {
    }

    public synchronized void addGattOperation(GattOperation gattOperation1) {
        Log.e(TAG, "addGattOperation(): " + gattOperation1);
        this.gattOperations.add(gattOperation1);
        this.start();
    }


    synchronized void start(){
        if (this.gattOperation == null && this.gattOperations.size() > 0) {
            GattOperation gattOperation1 = this.gattOperations.poll();
        }
    }

    public void removeGattOperation(BluetoothDevice bluetoothDevice) {
        for (GattOperation gattOperation2 : this.gattOperations) {
            if (!gattOperation.getBluetoothDevice().equals((Object)bluetoothDevice)) continue;
            if (this.getGattOperation() != null && this.getGattOperation().equals(gattOperation2)) {
                //
            }
            this.gattOperations.remove(gattOperation2);
        }
    }

    public GattOperation getGattOperation() {
        return this.gattOperation;
    }

    public ConcurrentHashMap<String, BluetoothGatt> getBluetoothGattMap() {
        return this.bluetoothGattMap;
    }

}
