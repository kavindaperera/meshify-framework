package com.codewizards.meshify.framework.controllers;

import com.codewizards.meshify.client.Device;
import com.codewizards.meshify.logs.Log;

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *  <p>This class manages all the connections</p>
 * @author Kavinda Perera
 * @version 1.0
 */
public class ConnectionManager {

    private static final String TAG = "[Meshify][ConnectionManager]";

    private static HashMap<String, Connection> connections = new HashMap<>();

    private static ConcurrentLinkedQueue<Device> devices = new ConcurrentLinkedQueue<>(); //unbounded thread-safe queue which arranges the element in FIFO

    private static MeshifyDevice meshifyDevice;

    /**
     * @return MeshifyDevice
     */
    public static MeshifyDevice getMeshifyDevice() {
        return meshifyDevice;
    }

    /**
     * @param meshifyDevice
     */
    static void setMeshifyDevice(MeshifyDevice meshifyDevice) {
        ConnectionManager.meshifyDevice = meshifyDevice;
    }

    /**
     * @param device
     * @return
     */
    static synchronized MeshifyDevice getConnectivity(Device device) {
        if (SessionManager.getSession(device.getDeviceAddress()) != null) {
            Log.d(TAG, "getConnectivity: already in session");
            return null;
        }

        if (ConnectionManager.getMeshifyDevice() != null && ConnectionManager.getMeshifyDevice().getDevice().equals(device)) {
            Log.d(TAG, "getConnectivity: already connecting to " + device.getDeviceAddress());
            return null;
        }

        switch (device.getAntennaType()) {
            case BLUETOOTH: {
                ConnectionManager.setMeshifyDevice(new BluetoothMeshifyDevice(device, false));
                break;
            }
            case BLUETOOTH_LE: {

            }
        }

        return meshifyDevice;

    }


}
