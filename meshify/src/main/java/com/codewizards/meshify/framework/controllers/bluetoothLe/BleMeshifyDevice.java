package com.codewizards.meshify.framework.controllers.bluetoothLe;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Build;

import com.codewizards.meshify.api.Config;
import com.codewizards.meshify.api.Device;
import com.codewizards.meshify.api.Meshify;
import com.codewizards.meshify.api.profile.DeviceProfile;
import com.codewizards.meshify.framework.controllers.bluetoothLe.gatt.GattOperation;
import com.codewizards.meshify.framework.controllers.bluetoothLe.gatt.operations.GattBatteryService;
import com.codewizards.meshify.framework.controllers.bluetoothLe.gatt.operations.GattDataService;
import com.codewizards.meshify.framework.controllers.connection.ConnectionManager;
import com.codewizards.meshify.framework.controllers.discoverymanager.BluetoothController;
import com.codewizards.meshify.framework.controllers.helper.BluetoothUtils;
import com.codewizards.meshify.framework.controllers.discoverymanager.DeviceManager;
import com.codewizards.meshify.framework.controllers.helper.MeshifyUtils;
import com.codewizards.meshify.framework.controllers.sessionmanager.Session;
import com.codewizards.meshify.framework.controllers.sessionmanager.SessionManager;
import com.codewizards.meshify.framework.controllers.sessionmanager.AbstractSession;
import com.codewizards.meshify.framework.controllers.discoverymanager.MeshifyDevice;
import com.codewizards.meshify.framework.controllers.bluetoothLe.gatt.BluetoothLeGatt;
import com.codewizards.meshify.framework.entities.MeshifyEntity;
import com.codewizards.meshify.logs.Log;

import java.lang.reflect.Method;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;

public class BleMeshifyDevice extends MeshifyDevice {

    final String TAG = "[Meshify][BleMeshifyDevice]";

    CompletableEmitter completableEmitter;

    GattCallback gattCallback = new GattCallback();


    public BleMeshifyDevice(Device device) {
        super(device);
    }

    @Override
    public Completable create() {
        return Completable.create(CompletableEmitter -> {
            this.completableEmitter = completableEmitter;
            BluetoothDevice bluetoothDevice = this.getDevice().getBluetoothDevice();
            if (Meshify.getInstance().getMeshifyCore() != null) {
                Log.i(TAG, "Connecting GATT " + this.getDevice().getBluetoothDevice().getAddress() + " " + this.getDevice().getUserId());
                BluetoothLeGatt bluetoothLeGatt = new BluetoothLeGatt(Meshify.getInstance().getMeshifyCore().getContext());
                bluetoothLeGatt.connectGatt(bluetoothDevice, false, this.gattCallback);
                Log.e(this.TAG, "connect as client device address: " + this.getDevice().getDeviceAddress());
                Session session = SessionManager.getSession(this.getDevice().getDeviceAddress());
                if (session == null) {
                    session = new Session(bluetoothDevice, true, this.completableEmitter);
                }
                session.setSessionId(this.getDevice().getDeviceAddress());
                this.getDevice().setSessionId(session.getSessionId());
                session.setDevice(this.getDevice());
                SessionManager.queueSession(session);
                DeviceManager.addDevice(session.getDevice());
            }
        });
    }

    private void sendInitialHandShake(Device device){
        Log.e(TAG, "sendInitialHandShake: " + device.getDeviceAddress());
        GattBatteryService gattBatteryService = new GattBatteryService(device.getBluetoothDevice(), BluetoothUtils.getBluetoothUuid(), BluetoothUtils.getCharacteristicUuid(), BluetoothUtils.batteryServiceUuid, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        BluetoothController.getGattManager().addGattOperation(gattBatteryService);
        Log.i(this.TAG, "1. Handshake request type 0 |  session: " + device.getSessionId());
        MeshifyEntity meshifyEntity = MeshifyEntity.generateHandShake();
        byte[] arrby = MeshifyUtils.marshall(meshifyEntity);
        Log.i(TAG, "length: " + arrby.length + " | write: " + arrby);
        GattDataService dataService = new GattDataService(device.getBluetoothDevice(), BluetoothUtils.getBluetoothUuid(), BluetoothUtils.getCharacteristicUuid(), arrby);
        BluetoothController.getGattManager().addGattOperation(dataService);
    }

    /**
     * The main interface that the app has to implement in order to receive callbacks for most BluetoothGatt-related operations
     * like reading, writing, or getting notified about incoming notifications or indications.
     */
    private class GattCallback extends BluetoothGattCallback {

        public GattCallback() {
        }

        @Override
        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status);
            Log.e(BleMeshifyDevice.this.TAG, "onPhyUpdate(): txPhy: " + txPhy + " | rxPhy: " + rxPhy + " | status: " + status);
        }

        @Override
        public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyRead(gatt, txPhy, rxPhy, status);
            Log.e(BleMeshifyDevice.this.TAG, "onPhyRead(): txPhy: " + txPhy + " | rxPhy: " + rxPhy + " | status: " + status);
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.e(BleMeshifyDevice.this.TAG,"onConnectionStateChange()" + " | newState: " + newState + " | status: " + status);
            try {
                synchronized (this) {
                    wait(1600);
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "onConnectionStateChange(): error " + e.getMessage());
            }
            if (status == BluetoothGatt.GATT_SUCCESS){

                // check bluetooth profile states
                if (newState == BluetoothProfile.STATE_CONNECTED) { // The profile is in connected state

                    Log.i(BleMeshifyDevice.this.TAG,"BluetoothProfile.STATE_CONNECTED: " + gatt.getDevice().getAddress());

                    // check and create a session
                    Session session = SessionManager.getSession(gatt.getDevice().getAddress());
                    if (session == null) {
                        Log.i(BleMeshifyDevice.this.TAG, "onConnectionStateChange(): create new session");
                        session = new Session(gatt);
                    }else {
                        session.setBluetoothGatt(gatt);
                        Log.i(BleMeshifyDevice.this.TAG, "onConnectionStateChange(): reusing previous empty session" + session.getSessionId());
                    }

                    // check and create a device
                    Device device = DeviceManager.getDevice(gatt.getDevice().getAddress());
                    if (device == null) {
                        device = new Device(gatt.getDevice(), true);
                    }
                    device.setAntennaType(Config.Antenna.BLUETOOTH_LE);
                    device.setSessionId(session.getSessionId());

                    session.setClient(true);
                    session.setDevice(device);

                    SessionManager.queueSession(session); // Queue the session before handshake
                    DeviceManager.addDevice(device);
                    BluetoothController.getGattManager().getBluetoothGattMap().put(gatt.getDevice().getAddress(), gatt);
                    if (Build.VERSION.SDK_INT >= 21) {
                        gatt.requestMtu(DeviceProfile.getMaxMtuSize()); // Requesting for a larger ATT MTU
                    } else {
                        gatt.discoverServices();
                    }

                } else if (newState == BluetoothProfile.STATE_DISCONNECTED){
                    Log.e(BleMeshifyDevice.this.TAG, "BluetoothProfile.STATE_DISCONNECTED: " + gatt.getDevice().getAddress());
                    String address = gatt.getDevice().getAddress();
                    Session session2 = SessionManager.getSession(address);
                    if (session2 == null || session2.getState() != 4) {
                        try {
                            if (gatt != null) {
                                gatt.close();
                            }
                        } catch (Exception e) {
                            //
                        }

                        BluetoothController.getGattManager().getBluetoothGattMap().remove(address);

                        if (BluetoothController.getGattManager().getGatt() != null && BluetoothController.getGattManager().getGatt().getBluetoothDevice().getAddress().equals(gatt.getDevice().getAddress())){
                            BluetoothController.getGattManager().start((GattOperation) null);
                        }

                        if (session2 != null) {
                            session2.removeSession();
                        }
                    }
                    BluetoothController.getGattManager().removeGattOperation(gatt.getDevice());
                    BluetoothController.getGattManager().start();
                }
            } else if (status == 133){
                Log.e(BleMeshifyDevice.this.TAG,  "GATT 133 Error!!" + gatt.getDevice().getAddress());

                if (gatt.getDevice() != null && gatt.getDevice().getAddress() != null) {
                    Log.e(BleMeshifyDevice.this.TAG,  "GATT 133 Error with " + gatt.getDevice().getAddress());
                }
                this.clearFailedConnection(gatt);

            } else {
                if (newState == BluetoothProfile.STATE_DISCONNECTED){
                    Log.e(BleMeshifyDevice.this.TAG, "BluetoothProfile.STATE_DISCONNECTED: " + gatt.getDevice().getAddress());
                    String address = gatt.getDevice().getAddress();
                    Session session2 = SessionManager.getSession(address);
                    if (session2 == null || session2.getState() != 4) {
                        try {
                            if (gatt != null) {
                                gatt.close();
                            }
                        } catch (Exception e) {
                            //
                        }
                        BluetoothController.getGattManager().getBluetoothGattMap().remove(address);
                        if (BluetoothController.getGattManager().getGatt() != null && BluetoothController.getGattManager().getGatt().getBluetoothDevice().getAddress().equals(gatt.getDevice().getAddress())){
                            BluetoothController.getGattManager().start((GattOperation) null);
                        }
                        if (session2 != null) {
                            session2.removeSession();
                        }
                    }
                    BluetoothController.getGattManager().removeGattOperation(gatt.getDevice());
                    BluetoothController.getGattManager().start();
                }
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.e(BleMeshifyDevice.this.TAG,"onMtuChanged()" + " | mtu: " + mtu + " | status: " + status);

            Session session = SessionManager.getSession(gatt.getDevice().getAddress());

            if (mtu == DeviceProfile.getMaxMtuSize()) {
                // change mtu in session
            }

            try {
                Thread.sleep(600);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Log.e(BleMeshifyDevice.this.TAG, "starting service discovery: " + gatt.discoverServices());

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.e(BleMeshifyDevice.this.TAG,"onServicesDiscovered()" + " | status: " + status + " | size: " + gatt.getServices().size());
            for (BluetoothGattService service : gatt.getServices()) {
                Log.i(BleMeshifyDevice.this.TAG,"service discovered " + service.getUuid().toString());
            }
            if (status == 0) {
                // DOUBLE_RATE
                if (!DeviceProfile.isLeDoubleRateSupported(Meshify.getInstance().getMeshifyCore().getContext())) return;
                gatt.setPreferredPhy(2, 2, 0);
                // TODO - create a config to set DOUBLE_RATE or EXTENDED_RANGE
                BluetoothGattCharacteristic bluetoothGattCharacteristic = gatt.getService(BluetoothUtils.getBluetoothUuid()).getCharacteristic(BluetoothUtils.getCharacteristicUuid());
                if (gatt.setCharacteristicNotification(bluetoothGattCharacteristic, true)){
                    BleMeshifyDevice.this.sendInitialHandShake(BleMeshifyDevice.this.getDevice());
                    BluetoothController.getGattManager().writeDescriptor(gatt, BluetoothController.getGattManager().getGatt());
                    return;
                }
                this.clearFailedConnection(gatt);
            } else {
                this.clearFailedConnection(gatt);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.e(BleMeshifyDevice.this.TAG,"onCharacteristicRead()" + " | characteristic: " + characteristic + " | status: " + status);
            //TODO
            BluetoothController.getGattManager().start(null);
            BluetoothController.getGattManager().start();
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.e(BleMeshifyDevice.this.TAG,"onCharacteristicWrite()" + " | characteristic: " + characteristic + " | status: " + status);
            BluetoothController.getGattManager().start(null);
            BluetoothController.getGattManager().start();
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.e(BleMeshifyDevice.this.TAG,"onCharacteristicChanged()" + " | characteristic: " + characteristic );

        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.e(BleMeshifyDevice.this.TAG,"onDescriptorRead()" + " | descriptor: " + descriptor + " | status: " + status);
            BluetoothController.getGattManager().start(null);
            BluetoothController.getGattManager().start();
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.e(BleMeshifyDevice.this.TAG,"onDescriptorWrite()" + " | descriptor: " + descriptor.getValue() + " | status: " + status);
            BluetoothController.getGattManager().start(null);
            BluetoothController.getGattManager().start();
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
            Log.e(BleMeshifyDevice.this.TAG,"onReliableWriteCompleted()" + " | status: " + status);

        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            Log.e(BleMeshifyDevice.this.TAG,"onReadRemoteRssi()" + " | rssi: " + rssi + " | status: " + status);

        }

        private void clearFailedConnection(BluetoothGatt bluetoothGatt) {
            BluetoothController.getGattManager().removeGattOperation(bluetoothGatt.getDevice());
            BluetoothController.getGattManager().getBluetoothGattMap().remove(bluetoothGatt.getDevice().getAddress());
            if (BluetoothController.getGattManager().getGatt() != null && BluetoothController.getGattManager().getGatt().getBluetoothDevice().getAddress().equals(bluetoothGatt.getDevice().getAddress())) {
                //
            }
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            this.refreshDeviceCache(bluetoothGatt);
            Device device = DeviceManager.getDevice(bluetoothGatt.getDevice().getAddress());
            Log.e(TAG, "clearFailedConnection(): address " + bluetoothGatt.getDevice().getAddress());
            Log.e(TAG, "clearFailedConnection(): queued device " + device);
            ConnectionManager.retry(device);
            SessionManager.removeSession(bluetoothGatt.getDevice().getAddress());
        }

        void refreshDeviceCache(BluetoothGatt bluetoothGatt) {
            try {
                Method localMethod  = bluetoothGatt.getClass().getMethod("refresh", new Class[0]);
                if (localMethod  != null) {
                    boolean bl = (Boolean)localMethod .invoke((Object)bluetoothGatt, new Object[0]);
                    Log.w(TAG, "refreshDeviceCache:" + bl);
                }
            }
            catch (Exception exception) {
                Log.e(TAG, "exception occurred while refreshing", exception);
            }
        }
    }

}
