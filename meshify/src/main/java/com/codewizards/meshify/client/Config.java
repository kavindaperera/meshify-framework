package com.codewizards.meshify.client;

public class Config {

    private boolean isEncryption;

    private Antenna antennaType;

    private ConfigProfile configProfile;

    private int maxConnectionRetries;

    private boolean isAutoConnect;

    public enum Antenna {
        BLUETOOTH,
        BLUETOOTH_LE,
        UNREACHABLE
    }


    public static final class Builder {

        private boolean isEncryption = false;

        private ConfigProfile configProfile = ConfigProfile.Default;

        private Antenna antennaType = Antenna.BLUETOOTH_LE;

        private int maxConnectionRetries = 10;

        private boolean isAutoConnect = true;

        public Config build(){
            return new Config(this);
        }

        public Builder setEncryption(boolean encryption) {
            this.isEncryption = encryption;
            return this;
        }

        public Builder setConfigProfile(ConfigProfile configProfile) {
            this.configProfile = configProfile;
            return this;
        }

        public Builder setAntennaType(Antenna antennaType) {
            this.antennaType = antennaType;
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

    public boolean isEncryption() {
        return this.isEncryption;
    }

    public ConfigProfile getConfigProfile() {
        return this.configProfile;
    }

    public Antenna getAntennaType() {
        return this.antennaType;
    }

    public int getMaxConnectionRetries() {
        return this.maxConnectionRetries;
    }

    public boolean isAutoConnect() {
        return this.isAutoConnect;
    }

    public void setAntennaType(Antenna antennaType) {
        this.antennaType = antennaType;
    }

    private Config(Builder builder) {
        this.isEncryption = builder.isEncryption;
        this.configProfile = builder.configProfile;
        this.antennaType = builder.antennaType;
        this.maxConnectionRetries = builder.maxConnectionRetries;
        this.isAutoConnect = builder.isAutoConnect;
    }


}
