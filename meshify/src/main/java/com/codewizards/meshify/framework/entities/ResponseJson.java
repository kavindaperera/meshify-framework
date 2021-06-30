package com.codewizards.meshify.framework.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;

public class ResponseJson implements Parcelable {

    @JsonProperty(value="type")
    private int type;

    @JsonProperty(value="key")
    private String key;

    @JsonProperty(value="uuid")
    private String uuid;

    public ResponseJson() {
    }

    protected ResponseJson(Parcel in) {
        type = in.readInt();
        uuid = in.readString();
        key = in.readString();
    }

    public static final Creator<ResponseJson> CREATOR = new Creator<ResponseJson>() {
        @Override
        public ResponseJson createFromParcel(Parcel in) {
            return new ResponseJson(in);
        }

        @Override
        public ResponseJson[] newArray(int size) {
            return new ResponseJson[size];
        }
    };

    public static ResponseJson ResponseTypeGeneral(String uuid) {
        ResponseJson responseJson = new ResponseJson();
        responseJson.setUuid(uuid);
        responseJson.setType(0);
        return responseJson;
    }

    public static ResponseJson ResponseTypeKey(String key) {
        ResponseJson responseJson = new ResponseJson();
        responseJson.setType(1);
        responseJson.setKey(key);
        return responseJson;
    }

    @JsonProperty(value="type")
    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @JsonProperty(value="uuid")
    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @JsonProperty(value="key")
    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String toString() {
        return new Gson().toJson((Object)this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type);
        dest.writeString(this.uuid);
        dest.writeString(this.key);
    }
}
