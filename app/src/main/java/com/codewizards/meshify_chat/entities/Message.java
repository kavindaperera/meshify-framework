package com.codewizards.meshify_chat.entities;

import com.google.gson.Gson;

public class Message {

    public final static int INCOMING_MESSAGE = 0;
    public final static int OUTGOING_MESSAGE = 1;

    private int direction;
    private String text;

    public Message(String text) {
        this.text = text;
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

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

}
