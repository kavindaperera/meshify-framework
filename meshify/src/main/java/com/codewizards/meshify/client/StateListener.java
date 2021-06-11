package com.codewizards.meshify.client;

public class StateListener {

    public static final int BLE_NOT_SUPPORTED = -10;
    public static final int INSUFFICIENT_PERMISSIONS = -20;
    public static final int LOCATION_SERVICES_DISABLED = -30;
    public static final int INITIALIZATION_ERROR = -40;

    public static final String INSUFFICIENT_LOCATION_PERMISSIONS_STRING = "ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION must be granted for Bluetooth connectivity to work";
    public static final String BLE_NOT_SUPPORTED_STRING = "BLE is not supported in this device";
    public static final String LOCATION_SERVICES_STRING = "Location Services must be enabled for BLE connectivity to work";
    public static final String INITIALIZATION_ERROR_STRING = "Meshify must be initialized before calling start()";
    public static final String INSUFFICIENT_BLUETOOTH_PERMISSIONS_STRING = "BLUETOOTH and BLUETOOTH_ADMIN permissions must be granted for Bluetooth Connectivity to work";


    public void onStartError(String message, int errorCode) {

    }

    public void onStarted() {

    }

}
