package com.codewizards.meshify.framework.controllers;

import android.os.Handler;
import android.os.Looper;

import com.codewizards.meshify.client.Device;
import com.codewizards.meshify.client.Meshify;
import com.codewizards.meshify.logs.Log;

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;

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

    static boolean checkConnection(String string) {
        Connection connection = connections.get(string);
        return connection != null && !connection.isConnected();
    }

    static void retry(Device device) {
        Connection connection = connections.get(device.getDeviceAddress());
        if (connection != null) {
            connection.setConnectionRetries(connection.getConnectionRetries() + 1);
        } else {
            connection = new Connection(false, Meshify.getInstance().getConfig().getMaxConnectionRetries());
        }
        connections.put(device.getDeviceAddress(), connection);
        ConnectionManager.retryConnection(device);
    }

    private static void retryConnection(Device device) {
        Connection connection = connections.get(device.getDeviceAddress());
        if (connection.getConnectionRetries() <= Meshify.getInstance().getConfig().getMaxConnectionRetries()) {
            int n2 = connection.getConnectionRetries() * 2 * 1000;
            Completable.timer(n2, TimeUnit.MILLISECONDS).subscribe(() -> {
                Log.i(TAG, "run: opening connection of device: " + device.getDeviceAddress() + " to retry");
                connection.setConnected(true);
                connections.put(device.getDeviceAddress(), connection);
            });
        } else {
            new Handler(Looper.getMainLooper()).post(() -> {
                if (Meshify.getInstance().getMeshifyCore() != null && Meshify.getInstance().getMeshifyCore().getConnectionListener() != null) {
                    Log.i(TAG, "onDeviceBlackListed:");
                    Meshify.getInstance().getMeshifyCore().getConnectionListener().onDeviceBlackListed(device);
                }
            });
        }
    }
}
