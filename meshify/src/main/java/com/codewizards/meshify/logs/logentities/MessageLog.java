package com.codewizards.meshify.logs.logentities;

import com.codewizards.meshify.api.Config;
import com.codewizards.meshify.api.Message;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class MessageLog extends LogEntity {

    String uuid;

    public MessageLog(Config.Antenna antenna, Message message, MessageEvent messageEvent) {
        super(LogType.MESSAGE, messageEvent.ordinal(), antenna);
        this.uuid = message.getUuid();
    }

    public MessageLog(Message message, String errorMessage) {
        super(LogEntity.LogType.MESSAGE_ERROR, MessageErrorEvent.DirectMessageNotSent.ordinal(), Config.Antenna.UNREACHABLE);
        this.uuid = message.getUuid();
        this.errorMessage = errorMessage;
    }

    public static MessageLog create(String serializedData) throws JsonSyntaxException {
        return (MessageLog) new Gson().fromJson(serializedData, MessageLog.class);
    }

    @Override
    public String serialize() {
        return new Gson().toJson((Object) this);
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    public static enum MessageEvent {
        DirectMessageSent,
        DirectMessageReceived
    }

    public static enum MessageErrorEvent {
        DirectMessageNotSent;
    }
}
