package com.codewizards.meshify.framework.entities;


import android.os.Parcel;
import android.os.Parcelable;

import com.codewizards.meshify.client.Meshify;
import com.codewizards.meshify.framework.utils.Utils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.Gson;

import java.util.UUID;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonDeserialize(using= MeshifyEntityDeserializer.class)
public class MeshifyEntity<T> implements Parcelable {

    @JsonProperty(value="id")
    private String id;
    @JsonProperty(value="entity")
    private int entity;
    @JsonProperty(value="content")
    private T content;

    protected MeshifyEntity() {
    }


    public MeshifyEntity(int et, T content) {
        this.entity = et;
        this.content = content;
        this.id = UUID.randomUUID().toString();
    }

    protected MeshifyEntity(Parcel in) {
        this.id = in.readString();
        this.entity = in.readInt();
        switch (entity) {
            case 0: {
                this.content = in.readParcelable(MeshifyHandshake.class.getClassLoader());
            }
        }
    }

    public static final Creator<MeshifyEntity> CREATOR = new Creator<MeshifyEntity>() {
        @Override
        public MeshifyEntity createFromParcel(Parcel in) {
            return new MeshifyEntity(in);
        }

        @Override
        public MeshifyEntity[] newArray(int size) {
            return new MeshifyEntity[size];
        }
    };

    public static MeshifyEntity generateHandShake() {
        return new MeshifyEntity<MeshifyHandshake>(0, new MeshifyHandshake(0, ResponseJson.ResponseTypeGeneral(Meshify.getInstance().getMeshifyClient().getUserUuid())));
    }

    public static MeshifyEntity generateHandShake(MeshifyHandshake meshifyHandshake) {
        return new MeshifyEntity<MeshifyHandshake>(0, meshifyHandshake);
    }

    public String toString() {
        return new Gson().toJson((Object)this);
    }


    @JsonProperty(value="entity")
    public int getEntity() {
        return this.entity;
    }

    public void setEntity(int entity) {
        this.entity = entity;
    }

    @JsonProperty(value="id")
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty(value="content")
    public T getContent() {
        return this.content;
    }

    public void setContent(T content) {
        this.content = content;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeInt(this.entity);
        if (this.content instanceof MeshifyHandshake) {
            dest.writeParcelable((Parcelable)((MeshifyHandshake)this.content), flags);
        }
    }
}
