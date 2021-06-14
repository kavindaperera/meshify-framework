package com.codewizards.meshify.client;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;

import com.codewizards.meshify.logs.Log;

public class MeshifyUtils {

    private static final String TAG = "[Meshify][MeshifyUtils]" ;

    private static boolean checkHardware(Context context) {
        return context.getPackageManager().hasSystemFeature("android.hardware.bluetooth_le");
    }

    public static BluetoothAdapter getBluetoothAdapter(Context context) {
        return ((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
    }

    public static boolean isLocationAvailable(Context context) {
        try {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager.isProviderEnabled("gps") || locationManager.isProviderEnabled("network")) {
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //Android things check
    public static boolean isThingsDevice(Context context) {
        PackageManager packageManager = context.getPackageManager();
        return packageManager.hasSystemFeature("android.hardware.type.embedded");
    }

    static void initialize(Context context, Config config) {
        switch (config.getAntennaType()) {
            case BLUETOOTH:
            case BLUETOOTH_LE: {
                MeshifyUtils.checkPermissions(context);
                if (config.getAntennaType() == Config.Antenna.BLUETOOTH_LE && !MeshifyUtils.checkHardware(context)) {
                    throw new MeshifyException(StateListener.BLE_NOT_SUPPORTED, StateListener.BLE_NOT_SUPPORTED_STRING);
                }
            }
        }
    }

    public static boolean checkLocationPermissions(Context context) {
        return context.checkCallingOrSelfPermission("android.permission.ACCESS_COARSE_LOCATION") == PackageManager.PERMISSION_GRANTED || context.checkCallingOrSelfPermission("android.permission.ACCESS_FINE_LOCATION") == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkBluetoothPermission(Context context) {
        return context.checkCallingOrSelfPermission("android.permission.ACCESS_COARSE_LOCATION") == PackageManager.PERMISSION_GRANTED || context.checkCallingOrSelfPermission("android.permission.ACCESS_FINE_LOCATION") == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    public static void enableBluetooth(Context context) {
        MeshifyUtils.getBluetoothAdapter(context).enable();
    }

    /**
     * Determine whether you have granted location and bluetooth  permissions to Meshify.
     * @param context
     * @throws MeshifyException
     */
    private static void checkPermissions(Context context) throws MeshifyException {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!checkLocationPermissions(context)) {
                throw new MeshifyException(StateListener.INSUFFICIENT_PERMISSIONS, StateListener.INSUFFICIENT_LOCATION_PERMISSIONS_STRING);
            } else if (!isLocationAvailable(context)) {
                throw new MeshifyException(StateListener.LOCATION_SERVICES_DISABLED, StateListener.LOCATION_SERVICES_STRING);
            }
        }

        if (!checkBluetoothPermission(context)) {
            throw new MeshifyException( StateListener.INSUFFICIENT_PERMISSIONS, StateListener.INSUFFICIENT_BLUETOOTH_PERMISSIONS_STRING);
        }
    }

}
