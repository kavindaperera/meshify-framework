package com.codewizards.meshify.client;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.util.HashMap;

public class Device implements Parcelable {

    private String deviceName;

    private String deviceAddress;

    private BluetoothDevice bluetoothDevice;

    private String userId;

    private Config.Antenna antennaType;

    private String sessionId;

    private String rssi;

    public Device() {
    }

    protected Device(Parcel in) {
        this.deviceName = in.readString();
        this.deviceAddress = in.readString();
        this.bluetoothDevice = (BluetoothDevice) in.readParcelable(BluetoothDevice.class.getClassLoader());
        this.userId = in.readString();
        this.sessionId = in.readString();
        this.antennaType = (Config.Antenna) in.readValue(Config.Antenna.class.getClassLoader());
    }

    public Device(String userId) {
        this.userId = userId;
    }

    public Device(String deviceName, String deviceAddress, String userId) {
        this.deviceName = deviceName;
        this.deviceAddress = deviceAddress;
        this.userId = userId;
    }

    @SuppressLint("MissingPermission")
    public Device(BluetoothDevice bluetoothDevice, boolean isBLE) {
        this.bluetoothDevice = bluetoothDevice;
        this.deviceName = bluetoothDevice.getName();
        this.deviceAddress = bluetoothDevice.getAddress();
        this.antennaType = isBLE ? Config.Antenna.BLUETOOTH_LE : Config.Antenna.BLUETOOTH;
    }

    public static final Creator<Device> CREATOR = new Creator<Device>() {
        @Override
        public Device createFromParcel(Parcel in) {
            return new Device(in);
        }

        @Override
        public Device[] newArray(int size) {
            return new Device[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.deviceName);
        dest.writeString(this.deviceAddress);
        dest.writeParcelable(this.bluetoothDevice, flags);
        dest.writeString(this.userId);
        dest.writeString(this.sessionId);
        dest.writeValue(this.antennaType);
    }

    public String sendMessage(@NonNull HashMap<String, Object> content) {
        Message.Builder builder = new Message.Builder();
        builder.setContent(content).setReceiverId(this.userId);
        return Meshify.sendMessage(builder.build());
    }

    public String sendMessage(@NonNull HashMap<String, Object> content, byte[] data) {
        Message.Builder builder = new Message.Builder();
        builder.setContent(content).setReceiverId(this.userId);
        builder.setData(data);
        return Meshify.sendMessage(builder.build());
    }

    /*getters*/

    public Config.Antenna getAntennaType() {
        return this.antennaType;
    }

    public BluetoothDevice getBluetoothDevice() {
        return this.bluetoothDevice;
    }

    public String getDeviceAddress() {
        return this.deviceAddress;
    }

    public String getDeviceName() {
        return this.deviceName;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getUserId() {
        return this.userId;
    }

    /*setters*/

    public void setAntennaType(Config.Antenna antennaType) {
        this.antennaType = antennaType;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
        this.deviceAddress = bluetoothDevice.getAddress();
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public synchronized void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String toString() {
        return new Gson().toJson(this);
    }

    public boolean equals(Object obj) {
        if (obj instanceof Device) {
            Device device = (Device) obj;
            return device.getDeviceAddress() != null && device.getDeviceAddress().trim().equalsIgnoreCase(getDeviceAddress().trim());
        }
        throw new IllegalArgumentException(obj.getClass().getName() + " is not a Device");
    }
}
