package com.codewizards.meshify.client;

import android.content.Context;
import android.os.Build;

import com.codewizards.meshify.logs.Log;

public class DeviceProfile {

    private static final String TAG = "[Meshify][DeviceProfile]" ;

    private String deviceEvaluation;

    private int rating;

    private DeviceCharacteristicsProfile deviceCharacteristicsProfile;

    enum DeviceCharacteristicsProfile {
        DeviceSupportsBluetoothClassic,
        DeviceSupportsBluetoothLeCentral,
        DeviceSupportsBluetoothLePeripheral,
        DeviceSupportsAllCharacteristics
    }

    public DeviceProfile(Context context) {
        checkDevice(context);
        setDeviceProfile(context);
        Log.d(TAG, "deviceEvaluation: " + this.deviceEvaluation);
        Log.d(TAG, "rating: " + this.rating);
        Log.d(TAG, "deviceCharacteristicsProfile: " + this.deviceCharacteristicsProfile);
    }

    private void checkDevice(Context context) {

        int i = Build.VERSION.SDK_INT;

        this.rating = i - 21;

        this.deviceEvaluation = getDeviceModel() + " Android SDK version " + i + " +" + this.rating + "\n";

        if (MeshifyUtils.getBluetoothAdapter(context).getBluetoothLeAdvertiser() != null) {
            this.rating++;
            this.deviceEvaluation += " Device can act as a client and a server +1\n";
        } else {
            this.deviceEvaluation += " Device can only act as a client \n";
        }

        if (MeshifyUtils.getBluetoothAdapter(context).isOffloadedFilteringSupported()) {
            this.rating++;
            this.deviceEvaluation += " Device can filter unwanted interference +1\n";
        } else {
            this.deviceEvaluation += " Device may experience interference from external sources \n";
        }

        if (MeshifyUtils.getBluetoothAdapter(context).isOffloadedScanBatchingSupported()) {
            this.rating++;
            this.deviceEvaluation += " Device can batch scan results +1\n";
        } else {
            this.deviceEvaluation += " Device cannot batch scan results \n";
        }

        if (i >= 26) {
            if (isLeExtendedRangeSupported(context)) {
                this.rating++;
                this.deviceEvaluation += " Device supports Bluetooth 5 LE Coded PHY features +1 \n";
            }
            if (isLeDoubleRateSupported(context)) {
                this.rating++;
                this.deviceEvaluation += " Device supports Bluetooth 5 LE 2M PHY features +1 \n";
            }
            if (isLeExtendedAdvertisingSupported(context)) {
                this.rating++;
                this.deviceEvaluation += " Device supports Bluetooth 5 Extended Advertising +1 \n";
            }
            if (isLePeriodicAdvertisingSupported(context)) {
                this.rating++;
                this.deviceEvaluation += " Device supports Bluetooth 5 Periodic Advertising +1 \n";
            }
        }

        if (deviceCanStopScan()) {
            this.rating++;
            this.deviceEvaluation += " Device can reset ongoing scans without side effects +1\n";
            return;
        }
        this.deviceEvaluation += " Device may not be able to reset ongoing scans\n";

    }

    private void setDeviceProfile(Context context) {
        if (!context.getPackageManager().hasSystemFeature("android.hardware.bluetooth_le")) {
            this.deviceCharacteristicsProfile = DeviceCharacteristicsProfile.DeviceSupportsBluetoothClassic;
        } else if (!isAdvertisingSupported(context)) {
            this.deviceCharacteristicsProfile = DeviceCharacteristicsProfile.DeviceSupportsBluetoothLeCentral;
        } else if (Build.VERSION.SDK_INT >= 23) {
            this.deviceCharacteristicsProfile = DeviceCharacteristicsProfile.DeviceSupportsAllCharacteristics;
        } else {
            this.deviceCharacteristicsProfile = DeviceCharacteristicsProfile.DeviceSupportsBluetoothLePeripheral;
        }
    }

    public static boolean isLeExtendedRangeSupported(Context context) {
        return Build.VERSION.SDK_INT >= 26 && MeshifyUtils.getBluetoothAdapter(context).isLeCodedPhySupported();
    }

    public static boolean isLeDoubleRateSupported(Context context) {
        return Build.VERSION.SDK_INT >= 26 && MeshifyUtils.getBluetoothAdapter(context).isLe2MPhySupported();
    }

    public static boolean isLeExtendedAdvertisingSupported(Context context) {
        return Build.VERSION.SDK_INT >= 26 && MeshifyUtils.getBluetoothAdapter(context).isLeExtendedAdvertisingSupported();
    }

    private static boolean isLePeriodicAdvertisingSupported(Context context) {
        return Build.VERSION.SDK_INT >= 26 && MeshifyUtils.getBluetoothAdapter(context).isLePeriodicAdvertisingSupported();
    }

    public static boolean deviceCanStopScan() {
        String str = Build.MANUFACTURER;
        str.hashCode();
        if (str.equals("unknown")) {
            return !Build.MODEL.equalsIgnoreCase("iot_rpi3");
        }
        return true;
    }

    public static int getMaxConnectionsForDevice() {
        return 1;
    }

    public String getDeviceEvaluation() {
        return deviceEvaluation;
    }

    public int getRating() {
        return rating;
    }

    public DeviceCharacteristicsProfile getDeviceCharacteristicsProfile() {
        return deviceCharacteristicsProfile;
    }

    public boolean isAdvertisingSupported(Context context) {
        return (MeshifyUtils.getBluetoothAdapter(context) == null || MeshifyUtils.getBluetoothAdapter(context).getBluetoothLeAdvertiser() == null) ? false : true;
    }

    private String getDeviceModel() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;

        if (model.startsWith(manufacturer)) {
            return Character.toUpperCase(model.charAt(0)) + model.substring(1).toLowerCase();
        }
        return manufacturer + " " + model;

    }

}
