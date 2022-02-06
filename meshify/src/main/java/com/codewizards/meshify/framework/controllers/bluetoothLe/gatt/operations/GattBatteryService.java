package com.codewizards.meshify.framework.controllers.bluetoothLe.gatt.operations;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import com.codewizards.meshify.framework.controllers.bluetoothLe.gatt.GattOperation;

import java.util.UUID;

public class GattBatteryService extends GattOperation {

    private final UUID serviceUuid;
    private final UUID characteristicUuid;
    private final UUID descriptorUuid;
    private byte[] descriptorValue;

    public GattBatteryService(BluetoothDevice bluetoothDevice, UUID serviceUuid, UUID characteristicUuid, UUID descriptorUuid, byte[] descriptorValue) {
        super(bluetoothDevice);
        this.serviceUuid = serviceUuid;
        this.characteristicUuid = characteristicUuid;
        this.descriptorUuid = descriptorUuid;
        this.descriptorValue = descriptorValue;
    }

    @Override
    public void writeDescriptor(BluetoothGatt bluetoothGatt) {
        BluetoothGattDescriptor bluetoothGattDescriptor;
        BluetoothGattCharacteristic bluetoothGattCharacteristic;
        BluetoothGattService bluetoothGattService = bluetoothGatt.getService(this.serviceUuid);
        if (bluetoothGattService != null && (bluetoothGattCharacteristic = bluetoothGattService.getCharacteristic(this.characteristicUuid)) != null && (bluetoothGattDescriptor = bluetoothGattCharacteristic.getDescriptor(this.descriptorUuid)) != null) {
            bluetoothGattDescriptor.setValue(this.descriptorValue);
            bluetoothGatt.writeDescriptor(bluetoothGattDescriptor);
        }
    }

    @Override
    public boolean isGatt() {
        return true;
    }

}
