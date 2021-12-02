package com.codewizards.meshify.framework.controllers;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.ParcelUuid;

import com.codewizards.meshify.client.Config;
import com.codewizards.meshify.client.Device;
import com.codewizards.meshify.client.Meshify;
import com.codewizards.meshify.logs.Log;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;

public class BluetoothLeDiscovery extends Discovery {

    private static String TAG = "[Meshify][BluetoothLeDiscovery]";

    private BluetoothAdapter bluetoothAdapter;

    private HashMap<String, String> deviceHashMap;

    private HashMap<String, ScheduledFuture> scheduledFutureHashMap;

    private CompositeDisposable disposable = new CompositeDisposable();

    private ScanCallback scanCallback;

    private ScheduledThreadPoolExecutor threadPoolExecutor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(40);

    private final String bleUuid = BluetoothUtils.getHashedBluetoothLeUuid(Meshify.getInstance().getConfig().isAutoConnect()).toString();   // j

    private final String btUuid = BluetoothUtils.getHashedBluetoothUuid(Meshify.getInstance().getConfig().isAutoConnect()).toString();      // k

    private final String bleUuid2 = BluetoothUtils.getHashedBluetoothLeUuid(Meshify.getInstance().getMeshifyClient().getApiKey());          // l



    public BluetoothLeDiscovery(Context context) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        this.bluetoothAdapter = bluetoothManager.getAdapter();
        if (this.bluetoothAdapter == null) {
            Log.e(TAG, " BluetoothAdapter was NULL");
        }
        this.deviceHashMap = new HashMap<>();
        this.scheduledFutureHashMap = new HashMap<>();
    }

    @SuppressLint("CheckResult")
    @Override
    void startDiscovery(Context context, Config config) {
        this.deviceFlowable = Flowable.create(new FlowableOnSubscribe<Device>() {
            @SuppressLint("MissingPermission")
            @Override
            public void subscribe(@NonNull FlowableEmitter<Device> emitter) throws Exception {
                if (BluetoothLeDiscovery.this.bluetoothAdapter != null && BluetoothLeDiscovery.this.bluetoothAdapter.isEnabled()) {
                    if (BluetoothLeDiscovery.this.bluetoothAdapter.getBluetoothLeScanner() == null) {
                        try {
                            Thread.sleep(500L);
                        } catch (InterruptedException interruptedException) {
                            interruptedException.printStackTrace();
                        }
                    }

                    try {
                        BluetoothLeDiscovery.this.bluetoothAdapter.getBluetoothLeScanner()
                                .startScan(
                                        BluetoothUtils.getBluetoothLeScanFilter(),
                                        BluetoothUtils.getScanSettings(),
                                        BluetoothLeDiscovery.this.scanCallback = new ScanCallback() {
                                            @Override
                                            public void onScanResult(int callbackType, ScanResult result) {
                                                super.onScanResult(callbackType, result);
                                                BluetoothLeDiscovery.this.onScanResultAction(result, (FlowableEmitter<Device>) emitter);
                                            }

                                            @Override
                                            public void onBatchScanResults(List<ScanResult> results) {
                                                super.onBatchScanResults(results);
                                                Log.d(TAG, "onBatchScanResults: ");
                                            }

                                            @Override
                                            public void onScanFailed(int errorCode) {
                                                super.onScanFailed(errorCode);
                                            }
                                        });

                    } catch (IllegalStateException illegalStateException) {
                        Log.e(TAG, "error: " + illegalStateException.getMessage());
                    }

                }

                //TODO

            }
        }, BackpressureStrategy.BUFFER);


        super.startDiscovery(context, config);

        Completable.timer(60L, TimeUnit.SECONDS).subscribe(() -> {
            if (SessionManager.sessionMap.isEmpty()) {
                Log.i(TAG, "startDiscovery: resetting");
                //TODO
            }
        }, throwable -> Log.e(TAG, "error: " + throwable.getMessage()));
    }

    @SuppressLint("MissingPermission")
    private void onScanResultAction(ScanResult scanResult, FlowableEmitter<Device> flowableEmitter) {
        String string = this.processScanResult(scanResult);
        if (string != null && this.bluetoothAdapter != null && this.bluetoothAdapter.isEnabled()){
            Device device = this.processPresenceResult(string, scanResult.getDevice());
            if (device != null && SessionManager.getSession(device.getDeviceAddress()) == null && Meshify.getInstance().getConfig().isAutoConnect()){
                flowableEmitter.onNext(device);
            } else if (device!=null){
                // TODO - add to scheduled future
            }
        }
    }

    @SuppressLint("MissingPermission")
    synchronized Device processPresenceResult(String string, BluetoothDevice bluetoothDevice) {
        if (this.bluetoothAdapter.isEnabled() && bluetoothDevice != null){
            if (ConnectionManager.checkConnection(bluetoothDevice.getAddress())){
                Log.e(TAG, "Device is blacklisted");
                return null;
            }
            if (SessionManager.sessionMap.get(bluetoothDevice.getAddress()) != null){
                return null;
            }
            String userUuid = Meshify.getInstance().getMeshifyClient().getUserUuid();
            boolean bl = false;
            Collection<Session> collection = SessionManager.sessionMap.values();
            for (Session session : collection){
                //TODO - get a session count
                if (session.getBluetoothGatt() != null && session.getBluetoothGatt().getDevice() != null && session.getBluetoothGatt().getDevice().equals(bluetoothDevice)){
                    bl = true;
                    break;
                }
            }
            if (!Meshify.getInstance().getConfig().isAutoConnect()) { //TODO - check maximum connections
                Device device = DeviceManager.getDevice(bluetoothDevice.getAddress());
                if (device == null) {
                    device = new Device(bluetoothDevice, true);
                    device.setAntennaType(Config.Antenna.BLUETOOTH_LE);
                    device.setDeviceName(string);
                    device.setUserId(string);
                }
                return device;
            }
            return null;
        }
        Log.e(TAG, "processPresenceResult: could not process Bluetooth device");
        return null;
    }

    private String processScanResult(ScanResult scanResult) {
        String string;
        blockX: {
            BluetoothDevice bluetoothDevice;
            Map<ParcelUuid,byte[]> map;
            blockY: {
                List list = scanResult.getScanRecord().getServiceUuids();
                map = scanResult.getScanRecord().getServiceData();
                bluetoothDevice = scanResult.getDevice();
                Log.i(TAG, "scanResult: " + map.toString());
                string = null;
                if (list == null || list.isEmpty()) break blockY;


                break blockX;
            }
            if (map == null || map.entrySet() == null) break blockX;
            for (Map.Entry entry : map.entrySet()) {
                String string3 = new String((byte[])entry.getValue());
                UUID uuid = ((ParcelUuid) entry.getKey()).getUuid();
                string = this.deviceHashMap.get(bluetoothDevice.getAddress());
                if (string != null) continue;
                Log.w(TAG, "\nAPIKEY: " + Meshify.getInstance().getMeshifyClient().getApiKey() +
                        "\nDeviceData: " + string3 +
                        "\nService UUID: " + uuid.toString() +
                        "\nCustom UUID: " + this.bleUuid2 +
                        "\nBT UUID: " + this.btUuid +
                        "\nBLE UUID: " + this.bleUuid);
                if (!uuid.toString().equalsIgnoreCase(this.bleUuid) && !uuid.toString().equalsIgnoreCase(this.btUuid) && !uuid.toString().equalsIgnoreCase(this.bleUuid2)) continue;
                string = BluetoothUtils.getUuidFromDataString(string3);
                this.deviceHashMap.put(bluetoothDevice.getAddress(), string);
                Log.e(TAG, "Device Found " + string3 + " userUuid: " + string);
            }
        }
        return string;
    }

}
