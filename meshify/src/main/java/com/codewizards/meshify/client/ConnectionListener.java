package com.codewizards.meshify.client;

public abstract class ConnectionListener {

    public void onStartError(String message, int errorCode) {

    }

    public void onStarted() {

    }

    public void onDeviceConnected(Device device, Session session) {

    }

    public void onDeviceBlackListed(Device device) {

    }

    public void onDeviceLost(Device device) {

    }

}
