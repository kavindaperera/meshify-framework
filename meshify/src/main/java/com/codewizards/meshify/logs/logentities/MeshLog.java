package com.codewizards.meshify.logs.logentities;

import com.codewizards.meshify.api.Config;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class MeshLog extends LogEntity{

    String uuid;

    int hops;

    public MeshLog(LogType logType, int eventId) {
        super(logType, eventId);
    }

    public MeshLog(LogType logType, int eventId, Config.Antenna connectionType) {
        super(logType, eventId, connectionType);
    }

    @Override
    public String serialize() {
        return new Gson().toJson((Object) this);
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    public static MeshLog create(String serializedData) throws JsonSyntaxException {
        return (MeshLog) new Gson().fromJson(serializedData, MeshLog.class);
    }

    public enum ErrorEvent {
        MeshMessageDiscard,
    }

    public enum Event {
        MeshMessageSent,
        MeshMessageReceivedToForward,
        MeshMessageReceivedToDestination,
        MeshMessageReceivedDuplicated,
    }

}
