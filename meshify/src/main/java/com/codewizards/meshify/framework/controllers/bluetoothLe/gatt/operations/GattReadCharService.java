package com.codewizards.meshify.framework.controllers.bluetoothLe.gatt.operations;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import com.codewizards.meshify.framework.controllers.bluetoothLe.gatt.GattOperation;
import com.codewizards.meshify.framework.controllers.discoverymanager.BluetoothController;
import com.codewizards.meshify.framework.controllers.helper.BluetoothUtils;
import com.codewizards.meshify.framework.controllers.sessionmanager.Session;
import com.codewizards.meshify.framework.controllers.sessionmanager.SessionManager;
import com.codewizards.meshify.logs.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class GattReadCharService extends GattOperation {

    final String TAG = "[Meshify][GattReadCharService]";

    private final UUID serviceUuid;
    private final UUID characteristicUuid;

    public GattReadCharService(BluetoothDevice bluetoothDevice, UUID serviceUuid, UUID characteristicUuid) {
        super(bluetoothDevice);
        this.serviceUuid = serviceUuid;
        this.characteristicUuid = characteristicUuid;
    }

    @Override
    public void writeDescriptor(BluetoothGatt bluetoothGatt) {
        BluetoothGattCharacteristic bluetoothGattCharacteristic = bluetoothGatt.getService(this.serviceUuid).getCharacteristic(this.characteristicUuid);
        boolean bl = bluetoothGatt.readCharacteristic(bluetoothGattCharacteristic);
    }

    @Override
    public boolean isGatt() {
        return true;
    }

    public void onRead(BluetoothGattCharacteristic bluetoothGattCharacteristic, BluetoothGatt bluetoothGatt) {
        synchronized (this){
            blockX2: for (Session session : SessionManager.getSessions()){
                if (session.getBluetoothGatt() == null || !session.getBluetoothGatt().equals(bluetoothGatt)) continue;
                if (bluetoothGattCharacteristic.getValue().length > 0){
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bluetoothGattCharacteristic.getValue());
                    int n1 = byteArrayInputStream.read();
                    int n2 = byteArrayInputStream.available();
                    byte[] arrby = new byte[n2];
                    byteArrayInputStream.read(arrby, 0, arrby.length);
                    Log.e(TAG, "n1 = " + n1);
                    if (n1 == 36) {
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        try {
                            byteArrayOutputStream.write(n2);
                            byteArrayOutputStream.write(arrby);
                        }
                        catch (IOException iOException) {
                            iOException.printStackTrace();
                        }
                        arrby =  byteArrayOutputStream.toByteArray();
                    }
                    session.getArrayList().add(arrby);
                    int n3;
                    byte[] arrby2 = bluetoothGattCharacteristic.getValue();
                    try {
                        n3 = arrby2[0];
                    } catch (Exception e) {
                        n3 = -1;
                    }
                    Log.e(TAG, "n3 = " + n3);
                    switch (n3){
                        case 36:{
                            GattReadCharService gattReadCharService = new GattReadCharService(
                                    bluetoothGatt.getDevice(),
                                    BluetoothUtils.getBluetoothUuid(),
                                    BluetoothUtils.getCharacteristicUuid());
                            BluetoothController.getGattManager().addGattOperation(gattReadCharService);
                        }
                    }
                    break;
                }
                Log.e(TAG, "characteristic with empty value!!");
                session.setArrayList(new ArrayList<byte[]>());
                break;
            }
        }
    }
}
