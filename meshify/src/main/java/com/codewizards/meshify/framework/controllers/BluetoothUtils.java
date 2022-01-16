package com.codewizards.meshify.framework.controllers;

import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.os.ParcelUuid;

import com.codewizards.meshify.client.Meshify;
import com.codewizards.meshify.framework.utils.Ascii85;
import com.codewizards.meshify.logs.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

    public static UUID batteryServiceUuid  = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private static UUID bluetoothUuid;

    private static UUID characteristicUuid;

    private static final char[] d;

    static {
        d = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    }

    public static UUID getBluetoothUuid() {
        if (bluetoothUuid == null) {
            bluetoothUuid = BluetoothUtils.getHashedBluetoothUuid(Meshify.getInstance().getConfig().isAutoConnect());
        }
        return bluetoothUuid;
    }


    public static UUID getCharacteristicUuid() {
        if (characteristicUuid == null) {
            characteristicUuid = BluetoothUtils.getHashedCharacteristicUuid(Meshify.getInstance().getMeshifyClient().getApiKey() + "CHARACTERISTIC");
            Log.i(TAG, "Bluetooth UUID characteristic is: " + characteristicUuid);
        }
        return characteristicUuid;
    }

    public static UUID getHashedBluetoothUuid(boolean bl) {
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

    public static UUID getHashedCharacteristicUuid(String string) {
        byte[] arrby = BluetoothUtils.getSHA(string);
        byte[] arrby2 = Arrays.copyOfRange(arrby, 0, 16);
        arrby2[arrby2.length - 1] = (byte)(Meshify.getInstance().getConfig().isAutoConnect() ? 255 : 238);
        ByteBuffer byteBuffer = ByteBuffer.wrap(arrby2);
        UUID uUID = new UUID(byteBuffer.getLong(), byteBuffer.getLong());
        return uUID;
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


    public static List<ScanFilter> getBluetoothLeScanFilter() {
        ArrayList<ScanFilter> arrayList = new ArrayList<ScanFilter>();
        ScanFilter.Builder builder = new ScanFilter.Builder();
        builder.setServiceUuid(new ParcelUuid(BluetoothUtils.getHashedBluetoothLeUuid(Meshify.getInstance().getConfig().isAutoConnect())));
        arrayList.add(builder.build());
        builder = new ScanFilter.Builder();
        builder.setServiceData(new ParcelUuid(BluetoothUtils.getHashedBluetoothLeUuid(Meshify.getInstance().getConfig().isAutoConnect())), null);
        arrayList.add(builder.build());
        return arrayList;
    }

    public static UUID getHashedBluetoothLeUuid(boolean bl) {
        UUID uUID = UUID.fromString(Meshify.getInstance().getMeshifyClient().getApiKey());
        UUID uUID2 = UUID.fromString("00000000-0000-1000-8000-00805f9b34fb");
        byte[] arrby = BluetoothUtils.getSignificantBits(uUID2.toString());
        byte[] arrby2 = Arrays.copyOfRange(BluetoothUtils.getSHA(uUID.toString()), 0, 16);
        arrby[2] = arrby2[arrby2.length - 2];
        arrby[3] = (byte)(bl ? 255 : 238);
        ByteBuffer byteBuffer = ByteBuffer.wrap(arrby);
        long l2 = byteBuffer.getLong();
        long l3 = byteBuffer.getLong();
        UUID uUID3 = new UUID(l2, l3);
        return uUID3;
    }

    public static String getHashedBluetoothLeUuid(String string) {
        UUID uUID = UUID.fromString(string);
        UUID uUID2 = UUID.fromString("00000000-0000-1000-8000-00805f9b34fb");
        byte[] arrby = BluetoothUtils.getSignificantBits(uUID2.toString());
        byte[] arrby2 = Arrays.copyOfRange(BluetoothUtils.getSHA(uUID.toString()), 0, 16);
        arrby[2] = arrby2[arrby2.length - 2];
        arrby[3] = (byte)(Meshify.getInstance().getConfig().isAutoConnect() ? 255 : 238);
        ByteBuffer byteBuffer = ByteBuffer.wrap(arrby);
        long l2 = byteBuffer.getLong();
        long l3 = byteBuffer.getLong();
        UUID uUID3 = new UUID(l2, l3);
        return uUID3.toString();
    }

    static byte[] getSignificantBits(String string) {
        UUID uUID = UUID.fromString(string);
        byte[] arrby = new byte[16];
        ByteBuffer.wrap(arrby).putLong(uUID.getMostSignificantBits()).putLong(uUID.getLeastSignificantBits());
        return arrby;
    }

    public static ScanSettings getScanSettings() {
        ScanSettings.Builder builder = new ScanSettings.Builder();
        if (Build.VERSION.SDK_INT >= 23) {
            builder.setNumOfMatches(1);
        }
        // TODO - add switch case
//        builder.setScanMode(0); // ENERGY_SAVER
        builder.setScanMode(1); // SCAN_MODE_BALANCED
//        builder.setScanMode(2); // HIGH_PERFORMANCE
        return builder.build();
    }

    private static byte[] getData(String string) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] arrby2 = BluetoothUtils.getSignificantBits(string);
        byteArrayOutputStream.write(arrby2);
        byte[] arrby3 = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();
        String string2 = Ascii85.encode(arrby3).trim();
        Log.i(TAG, "getAdvertiseData(): " + string2.getBytes().length + " bytes");
        return string2.getBytes();
    }

    public static AdvertiseData getAdvertiseData(String uuid) throws IOException{
        AdvertiseData.Builder builder = new AdvertiseData.Builder();
        builder.addServiceData(new ParcelUuid(BluetoothUtils.getHashedBluetoothLeUuid(Meshify.getInstance().getConfig().isAutoConnect())), BluetoothUtils.getData(uuid));
        builder.setIncludeDeviceName(false);
        return builder.build();
    }

    public static AdvertiseSettings getAdvertiseSettings() {
        AdvertiseSettings.Builder builder = new AdvertiseSettings.Builder();
        // TODO - add switch case
//        builder.setAdvertiseMode(0); //ENERGY_SAVER
        builder.setAdvertiseMode(1); // MODE_BALANCED
//        builder.setAdvertiseMode(2); //HIGH_PERFORMANCE
        builder.setConnectable(true);
        builder.setTimeout(0);
        return builder.build();
    }

    public static String getUuidFromDataString(String string) {
        Log.d(TAG, "DataString: " + string);
        String string2 = null;
        byte[] arrby = Ascii85.decode(string);
        if (arrby == null) {
            return string2;
        }
        byte[] arrby3 = Arrays.copyOfRange(arrby, 0, arrby.length);
        ByteBuffer byteBuffer = ByteBuffer.wrap(arrby3);
        Long l2 = byteBuffer.getLong();
        Long l3 = byteBuffer.getLong();
        UUID uUID = new UUID(l2, l3);
        return uUID.toString();
    }

}
