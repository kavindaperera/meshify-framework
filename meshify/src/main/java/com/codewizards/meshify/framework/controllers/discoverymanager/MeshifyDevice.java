package com.codewizards.meshify.framework.controllers.discoverymanager;

import androidx.annotation.Nullable;

import com.codewizards.meshify.api.Device;

import java.util.UUID;

import io.reactivex.Completable;

/**
 * <p>This class represents a general meshify device.</p>
 *
 * @author Kavinda Perera
 * @version 1.0
 */
public abstract class MeshifyDevice  implements Comparable {

    private String id;
    private Device device;

    public MeshifyDevice(Device device) {
        this.device = device;
        this.id = UUID.randomUUID().toString();
    }

    public abstract Completable create();

    public Device getDevice() {
        return device;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof MeshifyDevice) {
            return this.device.getDeviceAddress().equalsIgnoreCase(((MeshifyDevice)obj).getDevice().getDeviceAddress()) || this.device.getSessionId() == ((MeshifyDevice)obj).getDevice().getSessionId();
        }
        return false;
    }

    @Override
    public int compareTo(Object object) {
        return this.device.getDeviceAddress().compareTo(((MeshifyDevice)object).getDevice().getDeviceAddress());
    }

}
