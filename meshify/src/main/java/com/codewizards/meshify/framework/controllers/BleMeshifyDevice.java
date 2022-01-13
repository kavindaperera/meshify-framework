package com.codewizards.meshify.framework.controllers;


import com.codewizards.meshify.client.Device;

import io.reactivex.Completable;

public class BleMeshifyDevice extends MeshifyDevice {
    public BleMeshifyDevice(Device device) {
        super(device);
    }

    @Override
    public Completable create() {
        return null;
    }
}
