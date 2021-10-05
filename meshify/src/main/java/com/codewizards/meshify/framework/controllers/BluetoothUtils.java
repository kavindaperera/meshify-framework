package com.codewizards.meshify.framework.controllers;

import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.os.ParcelUuid;
import android.util.Log;

import com.codewizards.meshify.client.Meshify;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class BluetoothUtils {

    private static String TAG = "[Meshify][BluetoothUtils]";


    private static UUID uuidBluetooth;

    static UUID getBluetoothUuid() {
        if (uuidBluetooth == null) {
            uuidBluetooth = BluetoothUtils.getHashedBluetoothUuid(Meshify.getInstance().getConfig().isAutoConnect());
        }
        return uuidBluetooth;
    }

    static UUID getHashedBluetoothUuid(boolean bl) {
        UUID uUID = UUID.fromString(Meshify.getInstance().getMeshifyClient().getApiKey());
        byte[] arrby = BluetoothUtils.getSHA(uUID.toString()); //get SHA-256 Hash
        byte[] arrby2 = Arrays.copyOfRange(arrby, 0, 16);
        arrby2[2] = arrby2[arrby2.length - 2];
        arrby2[3] = (byte)(bl ? 255 : 238);
        arrby2[arrby2.length - 1] = (byte)(bl ? 255 : 238);
        ByteBuffer byteBuffer = ByteBuffer.wrap(arrby2);
        long l2 = byteBuffer.getLong();
        long l3 = byteBuffer.getLong();
        UUID uUID2 = new UUID(l2, l3);
        return uUID2;
    }


    private static byte[] getSHA(String string) {
        MessageDigest messageDigest = null;
        byte[] arrby = new byte[32];
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            arrby = messageDigest.digest(string.getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException | NoSuchAlgorithmException exception) {
            exception.printStackTrace();
        }
        messageDigest.reset();
        return arrby;
    }



}
