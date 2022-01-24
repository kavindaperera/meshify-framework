package com.codewizards.meshify.framework.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.codewizards.meshify.api.ConfigProfile;
import com.codewizards.meshify.api.Device;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MeshifyForwardHandshake implements Parcelable {

    private static  int[] hopLimits = new int[]{ 10, 0};
    private static int[] expiration = new int[]{15000, 10000};

    @JsonProperty(value="sender")
    String sender;

    @JsonProperty(value="profile")
    int profile;

    @JsonProperty(value="hops")
    int hops;

    @JsonProperty(value="neighborDetails")
    private ArrayList<Device> neighborDetails;

    @JsonIgnore
    Date added;

    public MeshifyForwardHandshake(String sender, ArrayList<Device> neighborDetails, ConfigProfile profile) {
        this.sender = sender;
        this.neighborDetails = neighborDetails;
        this.profile = profile.ordinal();
        this.hops = this.getHopLimitForConfigProfile();
        this.added = new Date(System.currentTimeMillis());
    }

    protected MeshifyForwardHandshake(Parcel in) {
        this.sender = in.readString();
        this.profile = in.readInt();
        this.hops = in.readInt();
        this.neighborDetails = in.readArrayList(Device.class.getClassLoader());
        this.added = new Date(in.readLong());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.sender);
        dest.writeInt(this.profile);
        dest.writeInt(this.hops);
        dest.writeList(this.neighborDetails);
        if (this.added == null) {
            this.added = new Date(System.currentTimeMillis());
        }
        dest.writeLong(this.added.getTime());
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
    public int getHopLimitForConfigProfile() { return hopLimits[this.profile]; }

    @JsonIgnore
    public int getExpirationForConfigProfile() { return expiration[this.profile]; }

    public void setHops(int hops) {
        this.hops = hops;
    }

    public int decreaseHops(){
        return --this.hops;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setAdded(Date added) {
        this.added = added;
    }

    public Date getAdded() {
        return this.added;
    }

    public String toString() {
        return new Gson().toJson((Object)this);
    }
}



