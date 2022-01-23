package com.codewizards.meshify.api;

public class Constants {

    public static final int BLE_NOT_SUPPORTED = -135;
    public static final String BLE_NOT_SUPPORTED_STRING = "BLE is not supported";

    public static final int INSUFFICIENT_PERMISSIONS = -246;
    public static final String INSUFFICIENT_LOCATION_PERMISSIONS_STRING = "ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION must be granted";
    public static final String INSUFFICIENT_BLUETOOTH_PERMISSIONS_STRING = "BLUETOOTH and BLUETOOTH_ADMIN permissions must be granted";

    public static final int LOCATION_SERVICES_DISABLED = -357;
    public static final String LOCATION_SERVICES_STRING = "Location Services must be enabled";

    public static final int INITIALIZATION_ERROR = -468;
    public static final String INITIALIZATION_ERROR_STRING = "Meshify must be initialized before starting";

    //Sequence Numbers
    public static long MIN_VALID_SEQ_NUM = 0;
    public static long MAX_VALID_SEQ_NUM = Long.MAX_VALUE;
    public static long UNKNOWN_SEQUENCE_NUMBER = 0;
    public static long FIRST_SEQUENCE_NUMBER = 1;

}
