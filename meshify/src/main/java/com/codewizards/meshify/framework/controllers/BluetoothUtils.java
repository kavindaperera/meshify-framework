package com.codewizards.meshify.framework.controllers;

import com.codewizards.meshify.client.Meshify;

import java.util.UUID;

public class BluetoothUtils {

    private static String TAG = "[Meshify][BluetoothUtils]";


    private static UUID uuidBluetooth;

    static UUID getBluetoothUuid() {
        if (uuidBluetooth == null) {
            uuidBluetooth = BluetoothUtils.getBluetoothUuid(Meshify.getInstance().getConfig().isAutoConnect());
        }
        return uuidBluetooth;
    }

    static UUID getBluetoothUuid(boolean bl) {
        UUID uUID = UUID.fromString(Meshify.getInstance().getMeshifyClient().getApiKey());
        return uUID;
    }

}
