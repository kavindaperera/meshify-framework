package com.codewizards.meshify.logs.logentities;

import com.codewizards.meshify.api.Config;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class ConnectionLog extends LogEntity{

    public ConnectionLog(Config.Antenna connectionType, String neighborName, Event event) {
        super(LogType.CONNECTION, event.ordinal(), connectionType);
        this.message = this.getMessage() + neighborName;
    }

    @Override
    public String serialize() {
        return new Gson().toJson(this);
    }

    public static ConnectionLog create(String serializedData) throws JsonSyntaxException {
        return new Gson().fromJson(serializedData, ConnectionLog.class);
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    public enum Event {
        NeighborDetected,
        ConnectionPerformed,
        NeighborConnected,
        NeighborDisconnected,
        HandshakeStarted,
        WaitingForHandshake,
        HandshakePacketSent,
        HandshakePacketReceived,
        HandshakeCompleted,
    }

    public enum ErrorEvent {
        ConnectionFailed,
    }

}
