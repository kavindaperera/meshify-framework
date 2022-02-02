package com.codewizards.meshify.api;

import java.util.Arrays;

public class Config {

    private boolean isEncryption;

    private boolean isVerified;

    private boolean neighborDiscovery;

    private Antenna[] antennaTypes;

    private ConfigProfile configProfile;

    private int maxConnectionRetries;

    private boolean isAutoConnect;

    private Config(Builder builder) {
        this.isEncryption = builder.isEncryption;
        this.isVerified = builder.isVerified;
        this.neighborDiscovery = builder.neighborDiscovery;
        this.configProfile = builder.configProfile;
        this.antennaTypes = builder.antennaTypes;
        this.maxConnectionRetries = builder.maxConnectionRetries;
        this.isAutoConnect = builder.isAutoConnect;
    }

    public boolean isEncryption() {
        return this.isEncryption;
    }

    public boolean isVerified() {
        return this.isVerified;
    }

    public ConfigProfile getConfigProfile() {
        return this.configProfile;
    }

    public Antenna[] getAntennaTypes() {
        return this.antennaTypes;
    }

    public boolean checkAntennaType(Antenna antenna) {
        return Arrays.asList(antennaTypes).contains(antenna);
    }

    public void setAntennaTypes(Antenna[] antennaTypes) {
        this.antennaTypes = antennaTypes;
    }

    public int getMaxConnectionRetries() {
        return this.maxConnectionRetries;
    }

    public boolean isAutoConnect() {
        return this.isAutoConnect;
    }

    public boolean isNeighborDiscovery() {
        return neighborDiscovery;
    }

    public enum Antenna {
        BLUETOOTH,
        BLUETOOTH_LE,
        UNREACHABLE
    }

    public static final class Builder {

        private boolean isEncryption = false;

        private boolean isVerified = false;

        private boolean neighborDiscovery = false;

        private ConfigProfile configProfile = ConfigProfile.Default;

        private Antenna[] antennaTypes = {Antenna.BLUETOOTH};

        private int maxConnectionRetries = 10;

        private boolean isAutoConnect = true;

        public Config build() {
            return new Config(this);
        }

        public Builder setEncryption(boolean encryption) {
            this.isEncryption = encryption;
            return this;
        }

        public Builder setVerified(boolean verified) {
            this.isVerified = verified;
            return this;
        }

        public Builder setNeighborDiscovery(boolean neighborDiscovery) {
            this.neighborDiscovery = neighborDiscovery;
            return this;
        }

        public Builder setConfigProfile(ConfigProfile configProfile) {
            this.configProfile = configProfile;
            return this;
        }

        public Builder setAntennaTypes(Antenna[] antennaTypes) {
            this.antennaTypes = antennaTypes;
            return this;
        }

        public Builder setMaxConnectionRetries(int maxConnectionRetries) {
            this.maxConnectionRetries = maxConnectionRetries;
            return this;
        }

        public Builder setAutoConnect(boolean autoConnect) {
            this.isAutoConnect = autoConnect;
            return this;
        }
    }


}
