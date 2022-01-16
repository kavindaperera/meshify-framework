package com.codewizards.meshify.client;

/**
 * This class allows to define callback functions for connection process
 */
public abstract class ConnectionListener {

    /**
     * Callback when there is an error at the start
     * @param message a string describing the error occurred
     * @param errorCode an integer which represents the error
     */
    public void onStartError(String message, int errorCode) {

    }

    /**
     * Callback when this is successfully started
     */
    public void onStarted() {

    }

    /**
     * Callback when a device is discovered successfully
     * @param device the Device object which was discovered
     */
    public void onDeviceDiscovered(Device device) {

    }


    /**
     * Callback when a device is connected successfully
     * @param device the Device object which was connected
     * @param session the Session object created for the device's connection
     */
    public void onDeviceConnected(Device device, Session session) {

    }

    /**
     * Callback when a device is black listed
     * @param device the Device object which was black listed
     */
    public void onDeviceBlackListed(Device device) {

    }

    /**
     * Callback when a device is lost due to connection loss
     * @param device the Device object which was lost
     */
    public void onDeviceLost(Device device) {

    }

}
