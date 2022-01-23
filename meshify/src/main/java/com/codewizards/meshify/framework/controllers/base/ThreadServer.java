package com.codewizards.meshify.framework.controllers.base;

import android.content.Context;

import com.codewizards.meshify.api.Config;
import com.codewizards.meshify.framework.expections.ConnectionException;

public abstract class ThreadServer<SOCKET, SERVER_SOCKET> extends Thread { //can be of type socket or server socket

    protected Context context;

    public final String TAG = getClass().getCanonicalName();

    private SERVER_SOCKET server_socket;

    private boolean isRunning = false;

    protected ThreadServer(Config config, Context context) throws ConnectionException {
        this.context = context;
    }

    public abstract void startServer() throws ConnectionException;

    public abstract void stopServer() throws ConnectionException;

    public abstract boolean alive();

    public SERVER_SOCKET getServerSocket() {
        return this.server_socket;
    }

    public void acceptConnection(SERVER_SOCKET server_socket) {
        this.server_socket = server_socket;
    }

    public void setRunning(boolean b) {
        this.isRunning = b;
    }

    public boolean isRunning() {
        return this.isRunning;
    }

}
