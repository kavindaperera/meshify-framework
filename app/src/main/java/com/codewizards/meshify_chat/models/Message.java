package com.codewizards.meshify_chat.models;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.Gson;

@Entity(tableName = "message_table",
        indices =
                {
                    @Index(value = {"receiverId"}),
                    @Index(value = {"senderId"})
                })
public class Message {

    @Ignore
    public final static int INCOMING_MESSAGE = 0;
    @Ignore
    public final static int OUTGOING_MESSAGE = 1;
    @Ignore
    public final static int INCOMING_BROADCAST__MESSAGE = 2;
    @Ignore
    public final static int OUTGOING_BROADCAST_MESSAGE = 3;

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "messageUuid")
    public String uuid;

    @ColumnInfo(name = "direction")
    private int direction;

    @ColumnInfo(name = "contents")
    private String message;

    @ColumnInfo(name = "dateSent")
    private String dateSent;

    @ColumnInfo(name = "senderId")
    public String senderId;

    @ColumnInfo(name = "receiverId")
    public String receiverId;

    @ColumnInfo(name = "userName")
    public String userName;


    public Message(String message, String senderId, String receiverId) {
        this.message = message;
        this.dateSent = String.valueOf(System.currentTimeMillis());
        this.senderId = senderId;
        this.receiverId = receiverId;
    }

    public Message(String messageUuid, String message, String senderId, String receiverId) {
        this.uuid = messageUuid;
        this.message = message;
        this.dateSent = String.valueOf(System.currentTimeMillis());
        this.senderId = senderId;
        this.receiverId = receiverId;
    }

    public Message(String messageUuid, String message, String senderId, String receiverId, String userName) {
        this.uuid = messageUuid;
        this.message = message;
        this.dateSent = String.valueOf(System.currentTimeMillis());
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.userName = userName;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public String getMessage() {
        return message;
    }

    public String getDateSent() {
        return dateSent;
    }

    public void setDateSent(String dateSent) {
        this.dateSent = dateSent;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Object can't be null.");
        }

        if (obj instanceof Message) {
            return ((Message) obj).getUuid() != null && ((Message) obj).getUuid().trim().equalsIgnoreCase(this.getUuid().trim());
        }

        throw new IllegalArgumentException(obj.getClass().getName() + " is not a " + this.getClass().getName());
    }

}
