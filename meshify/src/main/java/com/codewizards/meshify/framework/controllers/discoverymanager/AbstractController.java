package com.codewizards.meshify.framework.controllers.discoverymanager;

import android.content.Context;

import com.codewizards.meshify.framework.expections.ConnectionException;

abstract class AbstractController {

    AbstractController(){

    }

    abstract void startDiscovery(Context context);

    abstract void stopDiscovery(Context context);

    abstract void startServer(Context context) throws ConnectionException;

    abstract void stopServer() throws ConnectionException;

}
