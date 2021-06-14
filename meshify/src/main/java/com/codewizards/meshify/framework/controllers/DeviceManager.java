package com.codewizards.meshify.framework.controllers;

import android.os.Handler;
import android.os.Looper;

import com.codewizards.meshify.client.Device;
import com.codewizards.meshify.client.Meshify;
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
        Device device2 = DeviceManager.getDevice(device.getDeviceAddress());
        DeviceManager.copyDevice(device, device2);
        if (deviceList.put(device.getDeviceAddress(), device) == null) {
            Log.v(TAG, "::: Adding device: " + device.getDeviceAddress() + " : " + device.getAntennaType() + " to DeviceList size: " + deviceList.size());
        }
        DeviceManager.onDeviceConnected(device, session);
    }

    private static void onDeviceConnected(Device device, Session session) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (Meshify.getInstance().getMeshifyCore() != null && session != null && Meshify.getInstance().getMeshifyCore().getStateListener() != null) {
                Meshify.getInstance().getMeshifyCore().getStateListener().onDeviceConnected(device, session);
            }
        });
    }

    private static void copyDevice(Device device, Device device2) {
        if (device2 != null) {
            if (device.getSessionId() != null) {
                device2.setSessionId(device.getSessionId());
            } else if (device2.getSessionId() != null) {
                device.setSessionId(device2.getSessionId());
            }
            if (device.getUserId() != null) {
                device2.setUserId(device.getUserId());
            } else if (device2.getUserId() != null) {
                device.setUserId(device2.getUserId());
            }
            if (device.getCrc() > 0L) {
                device2.setCrc(device.getCrc());
            } else if (device2.getCrc() > 0L) {
                device.setCrc(device2.getCrc());
            }
        }
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
