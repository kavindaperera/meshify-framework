package com.codewizards.meshify.framework.controllers.bluetoothLe.gatt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.Context;
import android.os.Build;

import com.codewizards.meshify.logs.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BluetoothLeGatt {

    final String TAG = "[Meshify][BluetoothLeGatt]";

    private final Context context;

    public BluetoothLeGatt(Context context) {
        this.context = context;
    }

    public BluetoothGatt connectGatt(BluetoothDevice bluetoothDevice, boolean autoConnect, BluetoothGattCallback callback) {
        if (bluetoothDevice == null) {
            return null;
        }

        Log.e(TAG, "SDK VERSION : " + Build.VERSION.SDK_INT + " | autoConnect: " + autoConnect);

        if (Build.VERSION.SDK_INT >= 24 || !autoConnect) {
            return this.connectGatt(callback, bluetoothDevice, false);
        }

        try {
            Object object = this.getBluetoothGatt(this.getBluetoothManager());
            if (object == null) {
                return this.connectGatt(callback, bluetoothDevice, true);
            }
            BluetoothGatt bluetoothGatt = this.getBluetoothGatt(object, bluetoothDevice);
            if (bluetoothGatt == null) {
                return this.connectGatt(callback, bluetoothDevice, true);
            }
            boolean bl2 = this.connect(bluetoothGatt, callback, true);
            if (!bl2) {
                Log.w(TAG, "Connection using reflection failed, closing gatt");
                bluetoothGatt.close();
            }
            return bluetoothGatt;
        }
        catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchFieldException | NoSuchMethodException | InvocationTargetException exception) {
            return this.connectGatt(callback, bluetoothDevice, true);
        }

    }

    private BluetoothGatt connectGatt(BluetoothGattCallback bluetoothGattCallback, BluetoothDevice bluetoothDevice, boolean autoConnect) {
        if (Build.VERSION.SDK_INT >= 23) {
            return bluetoothDevice.connectGatt(this.context, autoConnect, bluetoothGattCallback, 0);
        }
        return bluetoothDevice.connectGatt(this.context, autoConnect, bluetoothGattCallback);
    }

    private boolean connect(BluetoothGatt bluetoothGatt, BluetoothGattCallback bluetoothGattCallback, boolean bl) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        this.declareField(bluetoothGatt, bl);
        Method method = bluetoothGatt.getClass().getDeclaredMethod("connect", Boolean.class, BluetoothGattCallback.class);
        method.setAccessible(true);
        return (Boolean)method.invoke((Object)bluetoothGatt, new Object[]{true, bluetoothGattCallback});
    }

    private BluetoothGatt getBluetoothGatt(Object object, BluetoothDevice bluetoothDevice) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<?> constructor = BluetoothGatt.class.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        if (constructor.getParameterTypes().length == 4) {
            return (BluetoothGatt)constructor.newInstance(new Object[]{this.context, object, bluetoothDevice, 2});
        }
        return (BluetoothGatt)constructor.newInstance(new Object[]{this.context, object, bluetoothDevice});
    }

    private Object getBluetoothGatt(Object object) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (object == null) {
            return null;
        }
        return this.getDeclaredMethod(object.getClass(), "getBluetoothGatt").invoke(object, new Object[0]);
    }

    private Object getBluetoothManager() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            return null;
        }
        return this.getDeclaredMethod(bluetoothAdapter.getClass(), "getBluetoothManager").invoke((Object)bluetoothAdapter, new Object[0]);
    }

    private Method getDeclaredMethod(Class<?> class_, String string) throws NoSuchMethodException {
        Method declaredMethod = class_.getDeclaredMethod(string, new Class[0]);
        declaredMethod.setAccessible(true);
        return declaredMethod;
    }

    private void declareField(BluetoothGatt bluetoothGatt, boolean bl) throws NoSuchFieldException, IllegalAccessException {
        Field declaredField = bluetoothGatt.getClass().getDeclaredField("mAutoConnect");
        declaredField.setAccessible(true);
        declaredField.setBoolean((Object)bluetoothGatt, bl);
    }

}
