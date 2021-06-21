package com.codewizards.meshify.client;

import android.content.Context;

import com.codewizards.meshify.logs.Log;

public class DeviceProfile {

    private static final String TAG = "[Meshify][DeviceProfile]" ;

    private String deviceEvaluation;

    private int rating;

    private CharacteristicsProfile characteristicsProfile;

    enum CharacteristicsProfile {
        SupportsBluetoothClassic,
        SupportsBluetoothLeCentral,
        SupportsBluetoothLePeripheral,
        SupportsAllCharacteristics
    }

    public DeviceProfile(Context context) {



    }

}
