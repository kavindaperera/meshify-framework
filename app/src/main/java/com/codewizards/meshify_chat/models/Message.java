package com.codewizards.meshify_chat.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Insert;
import androidx.room.PrimaryKey;

import com.google.gson.Gson;

@Entity(tableName = "message_table")
public class Message {

    @Ignore
    public final static int INCOMING_MESSAGE = 0;
    @Ignore
    public final static int OUTGOING_MESSAGE = 1;

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "messageId")
    public int id;

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

    public Message(String message, String senderId, String receiverId) {
        this.message = message;
        this.dateSent = String.valueOf(System.currentTimeMillis());
        this.senderId = senderId;
        this.receiverId = receiverId;
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
