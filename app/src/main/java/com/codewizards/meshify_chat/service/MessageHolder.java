package com.codewizards.meshify_chat.service;

import com.codewizards.meshify.client.Message;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;

public class MessageHolder {

    private String userId;

    private int notificationId;

    private ArrayList<Message> messages = new ArrayList<>();

    public MessageHolder(String str, ArrayList<Message> arrayList, int i) {
        this.userId = str;
        this.notificationId = i;
        if (arrayList != null) {
            this.messages = arrayList;
        }
    }

    public static MessageHolder create(String str) throws JsonSyntaxException {
        return (MessageHolder) new Gson().fromJson(str, MessageHolder.class);
    }

    public boolean equals(Object obj) {
        String str = this.userId;
        String str2 = ((MessageHolder) obj).userId;
        return str == null ? str2 == null : str.equals(str2);
    }

    public ArrayList<Message> getMessages() {
        if (this.messages == null) {
            this.messages = new ArrayList<>();
        }
        return this.messages;
    }

    public void setMessages(ArrayList<Message> arrayList) {
        this.messages = arrayList;
    }

    public int getNotificationId() {
        return this.notificationId;
    }

    public void setNotificationId(int i) {
        this.notificationId = i;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String str) {
        this.userId = str;
    }

    public int hashCode() {
        String str = this.userId;
        if (str != null) {
            return str.hashCode();
        }
        return 0;
    }

    public String serialize() {
        return new Gson().toJson(this);
    }
}
