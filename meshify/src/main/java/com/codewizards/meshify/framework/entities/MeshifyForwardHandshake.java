package com.codewizards.meshify.framework.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.codewizards.meshify.client.Device;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

public class MeshifyForwardHandshake implements Parcelable {

    @JsonProperty(value="sender")
    String sender;

    @JsonProperty(value="neighborDetails")
    private ArrayList<Device> neighborDetails;

    public MeshifyForwardHandshake(String sender, ArrayList<Device> neighborDetails) {
        this.sender = sender;
        this.neighborDetails = neighborDetails;
    }

    protected MeshifyForwardHandshake(Parcel in) {
        sender = in.readString();
        neighborDetails = in.readArrayList(Device.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sender);
        dest.writeList(this.neighborDetails);
    }

    public static final Creator<MeshifyForwardHandshake> CREATOR = new Creator<MeshifyForwardHandshake>() {
        @Override
        public MeshifyForwardHandshake createFromParcel(Parcel in) {
            return new MeshifyForwardHandshake(in);
        }

        @Override
        public MeshifyForwardHandshake[] newArray(int size) {
            return new MeshifyForwardHandshake[size];
        }
    };


    @JsonProperty(value="payload")
    public ArrayList<Device> getNeighborDetails() { return neighborDetails; }

    @JsonProperty(value="id")
    public String getSender() {
        return sender;
    }

    public void setSender(String id) {
        this.sender = id;
    }

    public String toString() {
        return new Gson().toJson((Object)this);
    }
}



