package com.codewizards.meshify.framework.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;

import java.util.HashMap;

public class MeshifyContent implements Parcelable {
    @JsonProperty(value="payload")
    private HashMap<String, Object> payload;
    @JsonProperty(value="id")
    private String id;

    public MeshifyContent(HashMap<String, Object> payload, String id) {
        this.payload = payload;
        this.id = id;
    }

    protected MeshifyContent(Parcel in) {
        id = in.readString();
        payload = (HashMap<String, Object>) in.readSerializable();
    }

    public static final Creator<MeshifyContent> CREATOR = new Creator<MeshifyContent>() {
        @Override
        public MeshifyContent createFromParcel(Parcel in) {
            return new MeshifyContent(in);
        }

        @Override
        public MeshifyContent[] newArray(int size) {
            return new MeshifyContent[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeSerializable(payload);
    }

    @JsonProperty(value="payload")
    public HashMap<String, Object> getPayload() {
        return payload;
    }

    @JsonProperty(value="id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String toString() {
        return new Gson().toJson((Object)this);
    }
}
