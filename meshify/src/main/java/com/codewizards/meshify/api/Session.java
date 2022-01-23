package com.codewizards.meshify.api;

public interface Session {

    String getPublicKey();

    String getUserId();

    Config.Antenna getAntennaType();

    void disconnect();

    boolean isClient();

}
