package com.codewizards.meshify_chat.models;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.codewizards.meshify.client.Device;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Entity(tableName = "neighbor_table")
public class Neighbor {

    @ColumnInfo(name = "neighborName")
    private String device_name;

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "neighborUuid")
    private String uuid;

    @ColumnInfo(name = "isNearby")
    private boolean isNearby;

    @ColumnInfo(name = "deviceType")
    private DeviceType deviceType;

    @Ignore
    private Device device;


    public Neighbor(@NonNull String uuid, String device_name) {
        this.uuid = uuid;
        this.device_name = device_name;
    }

    public void setDeviceName(String deviceName) {
        this.device_name = deviceName;
    }

    public String getDevice_name() {
        return this.device_name;
    }

    public String getUuid() {
        return uuid;
    }


    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public Device getDevice() {
        return this.device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public boolean isNearby() {
        return isNearby;
    }

    public void setNearby(boolean nearby) {
        isNearby = nearby;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public enum DeviceType {
        UNDEFINED,
        ANDROID
    }

}
