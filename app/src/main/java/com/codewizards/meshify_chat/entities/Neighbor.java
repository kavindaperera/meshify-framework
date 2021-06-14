package com.codewizards.meshify_chat.entities;

import com.google.gson.Gson;

public class Neighbor {
    private String  device_name;
    private String  uuid;
    private boolean isNearby;
    private DeviceType deviceType;

    public enum DeviceType {
        UNDEFINED,
        ANDROID
    }

    public Neighbor(String uuid, String device_name) {
        this.uuid = uuid;
        this.device_name = device_name;
    }

    public String getDeviceName() {
        return device_name;
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

    public boolean isNearby() {
        return isNearby;
    }

    public void setNearby(boolean nearby) {
        isNearby = nearby;
    }

    public static Neighbor create(String json) {
        return new Gson().fromJson(json, Neighbor.class);
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

}