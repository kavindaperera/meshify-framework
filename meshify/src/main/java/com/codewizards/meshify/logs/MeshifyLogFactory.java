package com.codewizards.meshify.logs;

import com.codewizards.meshify.api.Message;
import com.codewizards.meshify.api.Session;
import com.codewizards.meshify.framework.entities.MeshifyForwardEntity;
import com.codewizards.meshify.logs.logentities.LogEntity;
import com.codewizards.meshify.logs.logentities.MeshLog;
import com.codewizards.meshify.logs.logentities.MessageLog;

public class MeshifyLogFactory {

    public static LogEntity build(Message message, Session session, MessageLog.Event eventType) {
        if (session != null && session.getUserId() != null) {
            return new MessageLog(session.getAntennaType(), message, eventType);
        }
        return new MessageLog(message, "Session error occurred");
    }

    public static LogEntity build(Session session, MeshifyForwardEntity forwardEntity, MeshLog.Event event) {
        if (session != null && session.getUserId() != null) {
            return new MeshLog(event, forwardEntity);
        }
        return new MeshLog(MeshLog.ErrorEvent.InvalidSession, forwardEntity);
    }

    public static LogEntity build(MeshifyForwardEntity forwardEntity) {
        return new MeshLog(forwardEntity);
    }



}
