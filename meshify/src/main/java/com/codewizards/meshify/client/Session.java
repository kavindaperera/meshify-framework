package com.codewizards.meshify.client;

public interface Session {

    String getPublicKey();

    long getCrc();

    String getUserId();

    Config.Antenna getAntennaType();

    void disconnect();

}
