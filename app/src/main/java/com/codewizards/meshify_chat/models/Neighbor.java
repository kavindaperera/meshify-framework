package com.codewizards.meshify_chat.models;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.codewizards.meshify.client.Device;
import com.codewizards.meshify_chat.database.typeconveters.DeviceTypeConverter;
import com.google.gson.Gson;

@Entity(tableName = "neighbor_table")
public class Neighbor {

    @ColumnInfo(name = "neighborName")
    private String deviceName;

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "neighborUuid")
    private String uuid;

    @ColumnInfo(name = "isNearby")
    private boolean isNearby;

    @ColumnInfo(name = "deviceType")
    private DeviceType deviceType;

    @ColumnInfo(name = "device")
    @TypeConverters(DeviceTypeConverter.class)
    private Device device;

    @ColumnInfo(name = "lastSeen")
    private String lastSeen;

    public Neighbor(@NonNull String uuid, String deviceName) {
        this.uuid = uuid;
        this.deviceName = deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceName() {
        return this.deviceName;
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


    public String getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(String lastSeen) {
        this.lastSeen = lastSeen;
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
