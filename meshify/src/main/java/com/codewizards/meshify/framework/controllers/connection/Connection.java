package com.codewizards.meshify.framework.controllers.connection;

public class Connection { // [Layer] [Discovery Manager]

    private boolean isConnected;

    private int connectionRetries;

    Connection(boolean isConnected, int connectionRetries) {
        this.isConnected = isConnected;
        this.connectionRetries = connectionRetries;
    }

    public void setConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    public boolean isConnected() {
        return this.isConnected;
    }

    public int getConnectionRetries() {
        return this.connectionRetries;
    }

    public void setConnectionRetries(int connectionRetries) {
        this.connectionRetries = connectionRetries;
    }
}
