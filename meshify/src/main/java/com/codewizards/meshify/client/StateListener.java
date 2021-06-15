package com.codewizards.meshify.client;

public class StateListener {

    public static final int BLE_NOT_SUPPORTED = -135;
    public static final int INSUFFICIENT_PERMISSIONS = -246;
    public static final int LOCATION_SERVICES_DISABLED = -357;
    public static final int INITIALIZATION_ERROR = -468;

    public static final String INSUFFICIENT_LOCATION_PERMISSIONS_STRING = "ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION must be granted";
    public static final String BLE_NOT_SUPPORTED_STRING = "BLE is not supported on this device";
    public static final String LOCATION_SERVICES_STRING = "Location Services must be enabled for BLE connectivity to work";
    public static final String INITIALIZATION_ERROR_STRING = "Meshify must be initialized before starting";
    public static final String INSUFFICIENT_BLUETOOTH_PERMISSIONS_STRING = "BLUETOOTH and BLUETOOTH_ADMIN permissions must be granted for Bluetooth Connectivity to work";


    public void onStartError(String message, int errorCode) {

    }

    public void onStarted() {

    }

    public void onDeviceConnected(Device device, Session session) {

    }

    public void onDeviceBlackListed(Device device) {

    }

    public void onDeviceLost(Device device) {

    }

}
