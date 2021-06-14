package com.codewizards.meshify.framework.controllers;

import com.codewizards.meshify.client.Device;
import com.codewizards.meshify.logs.Log;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DeviceManager {

    private static final String TAG = "[Meshify][DeviceManager]";

    private static ConcurrentHashMap<String, Device> deviceList = new ConcurrentHashMap();

    private static ConcurrentHashMap<String, ScheduledFuture> futureConcurrentHashMap = new ConcurrentHashMap();

    private static ScheduledThreadPoolExecutor threadPoolExecutor = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() + 1);

    private DeviceManager() {
        try {
            threadPoolExecutor.awaitTermination(200L, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
    }

    static void addDevice(Device device) {
        addDevice(device, null);
    }

    static void addDevice(Device device, Session session) {
        Log.d(TAG, "addDevice: " + device + " | session: " + session);

    }

    public static Device getDevice(String deviceAddress) {
        ConcurrentHashMap<String, Device> concurrentHashMap = deviceList;
        synchronized (concurrentHashMap) {
            if (deviceList == null) {
                deviceList = new ConcurrentHashMap();
            }
            return deviceList.get(deviceAddress);
        }
    }

}
