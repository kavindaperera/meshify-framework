package com.codewizards.meshify.framework.controllers;

import android.os.Handler;
import android.os.Looper;

import com.codewizards.meshify.client.Device;
import com.codewizards.meshify.client.Meshify;
import com.codewizards.meshify.logs.Log;

import java.util.ArrayList;
import java.util.List;
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
        DeviceManager.addToScheduledFuture(device);
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

    static synchronized void removeDevice(Device device) {
        if (device != null) {
            Session session = SessionManager.getSession(device.getDeviceAddress());
            if (session != null && session.getState() == 1) {
                Log.w(TAG, "removeDevice: won't remove device because it still connecting: " + device.getDeviceAddress());
            } else if (deviceList.containsKey(device.getDeviceAddress())) {
                DeviceManager.cancelScheduledFuture(device);
                try {
                    device = deviceList.remove(device.getDeviceAddress());
                    if (device.getUserId() != null) {
                        Log.i(TAG, "removeDevice: on device lost " + device.getUserId());
                        DeviceManager.onDeviceLost(device);
                    } else {
                        Log.e(TAG, "removeDevice: not calling on device lost becuase userid was null");
                    }
                }
                catch (Exception exception) {
                    Log.e(TAG, "removeDevice: device could be null " + exception.getMessage());
                }
                Log.w(TAG, "Removing device: " + device.getDeviceAddress() + " (" + device.getUserId() + ") " + (Object)((Object)device.getAntennaType()) + ". DeviceList size: " + deviceList.size());
            }
        }
    }

    private static void onDeviceLost(Device device) {
        Log.i(TAG, "onDeviceLost: ");
        new Handler(Looper.getMainLooper()).post(() -> {
            if (Meshify.getInstance().getMeshifyCore() != null && Meshify.getInstance().getMeshifyCore().getStateListener() != null) {
                Log.i(TAG, "onDeviceLost:");
                Meshify.getInstance().getMeshifyCore().getStateListener().onDeviceLost(device);
            }
        });
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

    static synchronized void addToScheduledFuture(Device device) {
        ScheduledFuture scheduledFuture = DeviceManager.setupLongTimeout(device);
        futureConcurrentHashMap.put(device.getDeviceAddress(), scheduledFuture);
    }

    static synchronized void cancelScheduledFuture(Device device) {
        ScheduledFuture scheduledFuture = futureConcurrentHashMap.remove(device.getDeviceAddress());
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
    }

    static synchronized ScheduledFuture setupLongTimeout(Device device) {
        DeviceManager.cancelScheduledFuture(device);
        return threadPoolExecutor.schedule(() -> {
            Device device2 = device;
            synchronized (device2) {
                Session session = SessionManager.getSession(device.getSessionId());
                if (session == null) {
                    session = SessionManager.getSession(device.getDeviceAddress());
                }
                if (device.getSessionId() == null && session == null) {
                    Log.i(TAG, "setupLongTimeout: remove device because device is unconnected. " + device.getDeviceAddress());
                    DeviceManager.cancelScheduledFuture(device);
                    DeviceManager.removeDevice(device);
                }
            }
        }, 20000L, TimeUnit.MILLISECONDS);
    }

    static List<Device> getDeviceList() {
        return new ArrayList<Device>(deviceList.values());
    }

    static Device getDeviceByUserId(String userId) {
        for (Device device : DeviceManager.getDeviceList()) {
            if (device.getUserId() == null || !device.getUserId().equals(userId)) continue;
            return device;
        }
        return null;
    }

}
