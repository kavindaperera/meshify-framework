package com.codewizards.meshify.logs;

import com.codewizards.meshify.api.Message;
import com.codewizards.meshify.api.Session;
import com.codewizards.meshify.logs.logentities.LogEntity;
import com.codewizards.meshify.logs.logentities.MessageLog;

public class MeshifyLogFactory {

    public static LogEntity build(Message message, Session session, MessageLog.Event eventType) {
        if (session != null && session.getUserId() != null) {
            return new MessageLog(session.getAntennaType(), message, eventType);
        }
        return new MessageLog(message, "Session error occurred");
    }

}
