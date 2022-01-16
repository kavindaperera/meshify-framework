package com.codewizards.meshify.framework.controllers.bluetoothLe;

import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;

import com.codewizards.meshify.framework.controllers.BluetoothController;
import com.codewizards.meshify.logs.Log;

public class MeshifyAdvertiseCallback extends AdvertiseCallback {

    private static String TAG = "[Meshify][AdvertiseCallback]";

    public MeshifyAdvertiseCallback() {
        super();
    }

    @Override
    public void onStartSuccess(AdvertiseSettings settingsInEffect) {
        super.onStartSuccess(settingsInEffect);
        BluetoothController.state = 3;
        Log.i(TAG, "ADVERTISE_RUNNING | state = 3 \n" + settingsInEffect.toString() );
    }

    @Override
    public void onStartFailure(int errorCode) {
        super.onStartFailure(errorCode);
        switch (errorCode) {
            case 1: {
                Log.e(TAG, "Failed to start advertising as the advertise data to be broadcasted is larger than 31 bytes | ADVERTISE_FAILED_DATA_TOO_LARGE | code: " + errorCode);
                break;
            }
            case 2: {
                Log.e(TAG, "Failed to start advertising because no advertising instance is available | ADVERTISE_FAILED_TOO_MANY_ADVERTISERS | code: " + errorCode);
                break;
            }
            case 3: {
                Log.e(TAG, "Failed to start advertising as the advertising is already started | ADVERTISE_FAILED_ALREADY_STARTED | code: " + errorCode);
                break;
            }
            case 4: {
                Log.e(TAG, "Operation failed due to an internal error | ADVERTISE_FAILED_INTERNAL_ERROR | code: " + errorCode);
                break;
            }
            case 5: {
                Log.e(TAG, "This feature is not supported on this platform | ADVERTISE_FAILED_FEATURE_UNSUPPORTED | code: " + errorCode);
            }
        }
        BluetoothController.state = 0;
    }
}
