package com.codewizards.meshify.framework.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;

import java.util.List;

public class MeshifyForwardTransaction implements Parcelable {

    @JsonProperty(value = "sender")
    String sender;

    @JsonProperty(value = "mesh")
    List<MeshifyForwardEntity> mesh;

    @JsonProperty(value = "reach")
    String reach;

    public MeshifyForwardTransaction(String sender, List<MeshifyForwardEntity> mesh) {
        this.sender = sender;
        this.mesh = mesh;
    }

    protected MeshifyForwardTransaction(Parcel in) {
        this.sender = in.readString();
        this.mesh = in.createTypedArrayList(MeshifyForwardEntity.CREATOR);
        this.reach = in.readString();
    }

    public static final Creator<MeshifyForwardTransaction> CREATOR = new Creator<MeshifyForwardTransaction>() {
        @Override
        public MeshifyForwardTransaction createFromParcel(Parcel in) {
            return new MeshifyForwardTransaction(in);
        }

        @Override
        public MeshifyForwardTransaction[] newArray(int size) {
            return new MeshifyForwardTransaction[size];
        }
    };

    public MeshifyForwardTransaction() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.sender);
        dest.writeTypedList(this.mesh);
        dest.writeString(this.reach);
    }

    public String toString() {
        return new Gson().toJson(this);
    }

    @JsonProperty(value="sender")
    public String getSender() {
        return this.sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    @JsonProperty(value="mesh")
    public List<MeshifyForwardEntity> getMesh() {
        return this.mesh;
    }

    public void setMesh(List<MeshifyForwardEntity> mesh) {
        this.mesh = mesh;
    }

    @JsonProperty(value="reach")
    public String getReach() {
        return this.reach;
    }

    public void setReach(String reach) {
        this.reach = reach;
    }
}
