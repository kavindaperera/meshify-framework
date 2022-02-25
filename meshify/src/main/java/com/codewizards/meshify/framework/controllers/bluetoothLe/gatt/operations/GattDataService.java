package com.codewizards.meshify.framework.controllers.bluetoothLe.gatt.operations;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import com.codewizards.meshify.framework.controllers.bluetoothLe.gatt.GattOperation;
import com.codewizards.meshify.framework.controllers.transactionmanager.Transaction;

import java.util.UUID;

public class GattDataService extends GattOperation {

    private final UUID serviceUuid;
    private final UUID characteristicUuid;
    private final byte[] arrby;
    private Transaction transaction;

    public GattDataService(BluetoothDevice bluetoothDevice, UUID serviceUuid, UUID characteristicUuid, byte[] arrby) {
        super(bluetoothDevice);
        this.serviceUuid = serviceUuid;
        this.characteristicUuid = characteristicUuid;
        this.arrby = arrby;
    }

    @Override
    public void writeDescriptor(BluetoothGatt bluetoothGatt) {
        BluetoothGattService bluetoothGattService = bluetoothGatt.getService(this.serviceUuid);
        if (bluetoothGattService == null) {
            this.transaction.getByteArr().remove(this.arrby);
            if (this.transaction.getByteArr().size() == 0){
                // TODO - Error Handling
            }
            return;
        }
        BluetoothGattCharacteristic bluetoothGattCharacteristic = bluetoothGattService.getCharacteristic(this.characteristicUuid);
        bluetoothGattCharacteristic.setValue(this.arrby);
        bluetoothGattCharacteristic.setWriteType(2);
        bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);
        if (this.transaction != null) {
            this.transaction.getByteArr().remove(this.arrby);

            // TODO - can add a data progress callback

            if (this.transaction.getByteArr().size() == 0) {
                this.transaction.getTransactionManager().onTransactionFinished(this.transaction);
            }
        }
    }

    @Override
    public boolean isGatt() {
        return true;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

}
