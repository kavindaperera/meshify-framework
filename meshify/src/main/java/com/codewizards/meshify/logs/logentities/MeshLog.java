package com.codewizards.meshify.logs.logentities;

import com.codewizards.meshify.api.Config;
import com.codewizards.meshify.framework.entities.MeshifyForwardEntity;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class MeshLog extends LogEntity{

    String uuid;

    int hops;

    public MeshLog(Event event, MeshifyForwardEntity forwardEntity) {
        super(LogType.MESH, event.ordinal());
        this.uuid = String.valueOf(forwardEntity.getId());
        this.hops = forwardEntity.getHops();
    }

    public MeshLog(MeshifyForwardEntity forwardEntity) {
        super(LogType.MESH, Event.MeshMessageSent.ordinal());
        this.uuid = String.valueOf(forwardEntity.getId());
        this.hops = forwardEntity.getHops();
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

    public MeshLog(ErrorEvent errorEvent, MeshifyForwardEntity forwardEntity) {
        super(LogEntity.LogType.MESH_ERROR, errorEvent.ordinal());
        this.uuid = String.valueOf(forwardEntity.getId());
        this.hops = forwardEntity.getHops();
    }

    public enum ErrorEvent {
        MeshMessageDiscard,
        InvalidSession
    }

    public enum Event {
        MeshMessageSent,
        MeshMessageReceivedToForward,
        MeshMessageReceivedToDestination,
        MeshMessageReceivedDuplicated,
    }

}
