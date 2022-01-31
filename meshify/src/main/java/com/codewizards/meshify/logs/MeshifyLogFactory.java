package com.codewizards.meshify.logs;

import com.codewizards.meshify.api.Device;
import com.codewizards.meshify.api.Message;
import com.codewizards.meshify.api.Session;
import com.codewizards.meshify.framework.entities.MeshifyForwardEntity;
import com.codewizards.meshify.logs.logentities.ConnectionLog;
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

    // TODO - Add Connection log builders

    public static LogEntity build(Device device, ConnectionLog.Event connectionEvent) {
        ConnectionLog connectionLog = new ConnectionLog(device.getAntennaType(), device.getDeviceAddress(), connectionEvent);
        if (connectionLog.equals(ConnectionLog.Event.NeighborDetected)) {
            connectionLog.setMessage(connectionLog.getMessage() + "| " + device.getDeviceAddress());
        }
        return connectionLog;
    }

}
