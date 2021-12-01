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
import com.codewizards.meshify.logs.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private HashMap<String, String> stringStringHashMap;

    private HashMap<String, ScheduledFuture> stringScheduledFutureHashMap;

    private CompositeDisposable disposable = new CompositeDisposable();

    private ScanCallback scanCallback;

    private ScheduledThreadPoolExecutor threadPoolExecutor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(40);

    public BluetoothLeDiscovery(Context context) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        this.bluetoothAdapter = bluetoothManager.getAdapter();
        if (this.bluetoothAdapter == null) {
            Log.e(TAG, " BluetoothAdapter was NULL");
        }
        this.stringStringHashMap = new HashMap<>();
        this.stringScheduledFutureHashMap = new HashMap<>();
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
                                                Log.d(TAG, "onScanResult: ");
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
                                                Log.e(TAG, "onScanFailed: ");
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

    private void onScanResultAction(ScanResult result, FlowableEmitter<Device> emitter) {
        String string = this.processScanResult(result);
    }

    private String processScanResult(ScanResult scanResult) {
        String string;
        blockX: {
            BluetoothDevice bluetoothDevice;
            Map map;
            blockY: {
                List list = scanResult.getScanRecord().getServiceUuids();
                map = scanResult.getScanRecord().getServiceData();
                bluetoothDevice = scanResult.getDevice();
                Log.i(TAG, "service_data: " + map.toString());
                string = null;
                if (list == null || list.isEmpty()) break blockY;


                break blockX;
            }
            if (map == null || map.entrySet() == null) break blockX;

        }
        return string;
    }

}
