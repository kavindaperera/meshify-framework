package com.codewizards.meshify.framework.controllers;

import com.codewizards.meshify.client.Device;

import java.util.UUID;

import io.reactivex.Completable;

/**
 * <p>This class represents a general meshify device.</p>
 *
 * @author Kavinda Perera
 * @version 1.0
 */
abstract class MeshifyDevice  implements Comparable {

    private Device device;

    public MeshifyDevice(Device device) {
        this.device = device;
        UUID.randomUUID().toString();
    }

    public abstract Completable create();

    public Device getDevice() {
        return device;
    }

    @Override
    public int compareTo(Object object) {
        return this.device.getDeviceAddress().compareTo(((MeshifyDevice)object).getDevice().getDeviceAddress());
    }

}