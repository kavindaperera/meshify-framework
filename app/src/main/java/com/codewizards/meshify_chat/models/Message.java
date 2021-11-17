package com.codewizards.meshify_chat.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

import com.google.gson.Gson;

@Entity(tableName = "message_table")
public class Message {

    public final static int INCOMING_MESSAGE = 0;
    public final static int OUTGOING_MESSAGE = 1;

    @ColumnInfo(name = "direction")
    private int direction;

    @ColumnInfo(name = "message")
    private String message;

    @ColumnInfo(name = "dateSent")
    private String dateSent;

    public Message(String message) {
        this.message = message;
        this.dateSent = String.valueOf(System.currentTimeMillis());
    }

    public static Message create(String json) {
        return new Gson().fromJson(json, Message.class);
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

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

}
