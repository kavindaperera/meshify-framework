package com.codewizards.meshify.framework.controllers;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.util.Timer;

import io.reactivex.CompletableEmitter;

public class Session extends AbstractSession implements com.codewizards.meshify.client.Session, Comparable<Session> {

    private static final String TAG = "[Meshify][Session]";

    private int state = 0;  // 0 = disconnected | 2 = connected

    long createTime; //session start time

    private Timer timer; //schedule background timer

    private long crc; //Cyclic Redundancy Check - not added

    private String sessionId;

    public Session(BluetoothSocket bluetoothSocket) {
        super(bluetoothSocket);
    }

    public Session() {
    }

    void create() {

    }

    private void setCreateTime() {
        this.createTime = System.currentTimeMillis();
    }

    void removeSession() {

    }

    int getState() {
        return this.state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @Override
    public void setConnected(boolean connected) {
        super.setConnected(connected);
        if (connected) {
            this.setState(2);
        } else {
            this.setState(0);
        }
    }

    public void setCrc(long crc) {
        this.crc = crc;
    }

    @Override
    public long getCrc() {
        return this.crc;
    }

    @Override
    public void disconnect() {

    }

    public boolean equals(Object obj) {
        if (obj instanceof Session) {
            return this.getSessionId().equals(((Session)obj).getSessionId());
        }
        return false;
    }

    @Override
    public int compareTo(Session session) {
        return String.valueOf(this.getCrc()).compareTo(String.valueOf(session.getCrc()));
    }

    public String getSessionId() {
        return this.sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
