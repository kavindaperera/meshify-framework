package com.codewizards.meshify.client;

public interface Session {

    String getPublicKey();

    String getUserId();

    Config.Antenna getAntennaType();

    void disconnect();

    boolean isClient();

}
