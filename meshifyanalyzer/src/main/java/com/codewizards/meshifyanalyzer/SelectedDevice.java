package com.codewizards.meshifyanalyzer;


import com.codewizards.meshify.api.Device;

public class SelectedDevice{

    Device device;

    private boolean selected;

    public SelectedDevice(Device device) {
        this.device = device;
        this.selected = false;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String toString() {
        return this.device.getDeviceName() + " - " + device.getUserId();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Object can't be null.");
        }

        if (obj instanceof SelectedDevice) {
            return ((SelectedDevice) obj).device.getUserId() != null && ((SelectedDevice) obj).device.getUserId().trim().equalsIgnoreCase(this.device.getUserId().trim());
        }

        throw new IllegalArgumentException(obj.getClass().getName() + " is not a " + this.getClass().getName());

    }

}
