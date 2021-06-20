package com.codewizards.meshify_chat.entities;

import com.codewizards.meshify.client.Device;
import com.google.gson.Gson;

public class Neighbor {
    private String device_name;
    private String uuid;
    private boolean isNearby;
    private DeviceType deviceType;
    private Device device;

    public Neighbor(String uuid, String device_name, Device device) {
        this.uuid = uuid;
        this.device_name = device_name;
        this.device = device;
    }

    public static Neighbor create(String json) {
        return new Gson().fromJson(json, Neighbor.class);
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
