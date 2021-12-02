package com.codewizards.meshify.client;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.HashMap;
import java.util.UUID;

public class Message implements Parcelable {

    private HashMap content;

    private String receiverId;

    private String senderId;

    private String uuid;

    private long dateSent;

    private byte[] data;

    private boolean isMesh;

    private int hop;

    private int hops = 0;

    public Message(HashMap<String, Object> content, String receiverId, String senderId, boolean mesh, int hop) {
        this.content = content;
        this.receiverId = receiverId;
        this.senderId = senderId;
        this.dateSent = System.currentTimeMillis();
        this.uuid = UUID.randomUUID().toString();
        this.isMesh = mesh;
        this.hop = hop;
    }

    public static Message create(String str) throws JsonSyntaxException {
        return (Message) new Gson().fromJson(str, Message.class);
    }


    protected Message(Parcel parcel) {
        receiverId = parcel.readString();
        senderId = parcel.readString();
        uuid = parcel.readString();
        dateSent = parcel.readLong();
        data = parcel.createByteArray();
        isMesh = parcel.readByte() != 0;
        hop = parcel.readInt();
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    private Message(Builder builder) {
        this.content = builder.content;
        this.receiverId = builder.receiverId;
        this.senderId = Meshify.getInstance().getMeshifyClient().getUserUuid();
        this.dateSent = System.currentTimeMillis();
        this.uuid = UUID.randomUUID().toString();
        this.data = builder.data;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static class Builder {

        private HashMap<String, Object> content;

        private String receiverId;

        private byte[] data;

        public Message build() {
            return new Message(this);
        }

        public Builder setContent(HashMap<String, Object> content) {
            this.content = content;
            return this;
        }

        public Builder setReceiverId(String receiverId) {
            this.receiverId = receiverId;
            return this;
        }

        public Builder setData(byte[] data) {
            this.data = data;
            return this;
        }
    }

    public HashMap getContent() {
        return content;
    }

    public byte[] getData() {
        return data;
    }

    public long getDateSent() {
        return dateSent;
    }

    public int getHop() {
        return hop;
    }

    public int getHops() {
        return hops;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getUuid() {
        return uuid;
    }

    public boolean isMesh() {
        return isMesh;
    }

    public void setContent(HashMap content) {
        this.content = content;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setDateSent(long dateSent) {
        this.dateSent = dateSent;
    }

    public void setMesh(boolean mesh) {
        isMesh = mesh;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj != null && obj instanceof Message) {
            return ((Message) obj).getUuid() != null && ((Message) obj).getUuid().trim().equalsIgnoreCase(this.getUuid().trim());
        }
        throw new IllegalArgumentException(obj.getClass().getCanonicalName() + " is not a instance of " + Message.class.getName());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(receiverId);
        dest.writeString(senderId);
        dest.writeString(uuid);
        dest.writeLong(dateSent);
        dest.writeByteArray(data);
        dest.writeByte((byte) (isMesh ? 1 : 0));
        dest.writeInt(hop);
        dest.writeInt(hops);
    }


}
