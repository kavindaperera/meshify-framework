package com.codewizards.meshify.framework.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.codewizards.meshify.client.ConfigProfile;
import com.codewizards.meshify.client.Device;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

public class MeshifyForwardHandshake implements Parcelable {

    private static  int[] hopLimits = new int[]{ 10, 0};

    @JsonProperty(value="sender")
    String sender;

    @JsonProperty(value="hops")
    int hops;

    @JsonProperty(value="profile")
    int profile;

    @JsonProperty(value="neighborDetails")
    private ArrayList<Device> neighborDetails;

    public MeshifyForwardHandshake(String sender, ArrayList<Device> neighborDetails, ConfigProfile profile) {
        this.sender = sender;
        this.neighborDetails = neighborDetails;
        this.hops = this.getHopLimitForConfigProfile();
        this.profile = profile.ordinal();
    }

    protected MeshifyForwardHandshake(Parcel in) {
        sender = in.readString();
        hops = in.readInt();
        profile = in.readInt();
        neighborDetails = in.readArrayList(Device.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sender);
        dest.writeInt(hops);
        dest.writeInt(profile);
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


    @JsonProperty(value="neighborDetails")
    public ArrayList<Device> getNeighborDetails() { return neighborDetails; }

    @JsonProperty(value="sender")
    public String getSender() {
        return sender;
    }

    @JsonProperty(value="hops")
    public int getHops() {
        return this.hops;
    }

    @JsonProperty(value="profile")
    public int getProfile() {
        return this.profile;
    }

    public void setProfile(int profile) {
        this.profile = profile;
    }

    @JsonIgnore
    public int getHopLimitForConfigProfile() {
        return hopLimits[this.profile];
    }

    public void setHops(int hops) {
        this.hops = hops;
    }

    public int decreaseHops(){
        return --this.hops;
    }

    public void setSender(String id) {
        this.sender = id;
    }

    public String toString() {
        return new Gson().toJson((Object)this);
    }
}



