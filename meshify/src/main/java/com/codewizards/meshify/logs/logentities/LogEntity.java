package com.codewizards.meshify.logs.logentities;

import com.codewizards.meshify.api.Config;

public abstract class LogEntity {

    int logType;

    int eventId;

    int connectionType;

    long timeStamp = System.currentTimeMillis();

    String message;

    String errorMessage;

    public LogEntity(LogType logType, int eventId) {
        this.logType = logType.ordinal();
        this.eventId = eventId;
        this.message = this.getMessage();
    }

    public LogEntity(LogType logType, int eventId, Config.Antenna connectionType) {
        this.logType = logType.ordinal();
        this.eventId = eventId;
        this.connectionType = connectionType.ordinal();
        this.message = this.getMessage();
    }

    public abstract String serialize();

    public abstract String getMessage();

    public void setMessage(String message) {
        this.message = message;
    }

    public int getLogType() {
        return logType;
    }

    public static enum LogLevel {
        VERBOSE,
        DEBUG,
        INFO,
        WARN,
        ERROR,
        ASSERT
    }

    public static enum LogType {
        MESSAGE,
        MESSAGE_ERROR,
        MESH,
        MESH_ERROR,
        CONNECTION,
        CONNECTION_ERROR,
    }

}
