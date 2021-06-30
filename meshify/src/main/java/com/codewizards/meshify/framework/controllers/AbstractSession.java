package com.codewizards.meshify.framework.controllers;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothSocket;

import com.codewizards.meshify.client.Config;
import com.codewizards.meshify.client.Device;
import com.codewizards.meshify.framework.utils.Utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import io.reactivex.CompletableEmitter;

public abstract class AbstractSession {

    protected final String TAG = "[Meshify][AbstractSession]";

    private BluetoothDevice bluetoothDevice;

    private ArrayList<byte[]> arrayList = new ArrayList();

    private BluetoothSocket bluetoothSocket;

    private Socket socket;

    private String uuid;

    private Device device;

    private Config.Antenna antennaType;

    private boolean isConnected = false;

    private DataOutputStream dataOutputStream;

    private DataInputStream dataInputStream;

    private String userId;

    private String publicKey;

    private boolean isClient;

    CompletableEmitter emitter;

    public CompletableEmitter getEmitter() {
        return this.emitter;
    }

    AbstractSession(BluetoothSocket bluetoothSocket) {
        this.setBluetoothSocket(bluetoothSocket);
        this.setAntennaType(Config.Antenna.BLUETOOTH);
        this.uuid = Utils.generateSessionId();
    }

    AbstractSession(Socket socket, Config.Antenna antenna) {
        this.setSocket(socket);
        this.setAntennaType(antenna);
        this.uuid = Utils.generateSessionId();
    }

    AbstractSession(Config.Antenna antenna) {
        this.setAntennaType(antenna);
    }

    AbstractSession(BluetoothDevice bluetoothDevice, boolean z, CompletableEmitter completableEmitter) {
        this.emitter = completableEmitter;
        setBluetoothDevice(bluetoothDevice);
        if (z) {
            setAntennaType(Config.Antenna.BLUETOOTH_LE);
        } else {
            setAntennaType(Config.Antenna.BLUETOOTH);
        }
    }

    AbstractSession() {

    }

    public BluetoothSocket getBluetoothSocket() {
        return this.bluetoothSocket;
    }

    public void setBluetoothSocket(BluetoothSocket bluetoothSocket) {
        this.bluetoothSocket = bluetoothSocket;
    }

    public void setConnected(boolean connected) {
        this.isConnected = connected;
    }

    public ArrayList<byte[]> getArrayList() {
        return this.arrayList;
    }

    public Socket getSocket() {
        return this.socket;
    }

    public DataOutputStream getDataOutputStream() {
        return this.dataOutputStream;
    }

    public DataInputStream getDataInputStream() {
        return this.dataInputStream;
    }

    public Config.Antenna getAntennaType() {
        return this.antennaType;
    }

    public BluetoothDevice getBluetoothDevice() {
        return this.bluetoothDevice;
    }

    public String getUuid() {
        return this.uuid;
    }

    public Device getDevice() {
        return this.device;
    }

    public boolean isConnected() {
        return this.isConnected;
    }

    public String getUserId() {
        return this.userId;
    }

    public String getPublicKey() {
        return this.publicKey;
    }

    public boolean isClient() {
        return this.isClient;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setAntennaType(Config.Antenna antennaType) {
        this.antennaType = antennaType;
    }

    public void setDataOutputStream(DataOutputStream dataOutputStream) {
        this.dataOutputStream = dataOutputStream;
    }

    public void setDataInputStream(DataInputStream dataInputStream) {
        this.dataInputStream = dataInputStream;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }



    public synchronized void setArrayList(ArrayList<byte[]> arrayList) {
        this.arrayList = arrayList;
    }

    public void setClient (boolean z) {
        this.isClient = z;
    }

    public void setUserId(String str) {
        this.userId = str;
    }
}
