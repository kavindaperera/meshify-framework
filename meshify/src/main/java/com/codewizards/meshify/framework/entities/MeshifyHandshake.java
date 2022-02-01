package com.codewizards.meshify.framework.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;

public class MeshifyHandshake{

    @JsonProperty(value="rq")
    private Integer rq;  //Request
    @JsonProperty(value="rp")
    private ResponseJson rp; //Response

    public MeshifyHandshake() {
    }

    public MeshifyHandshake(@Nullable Integer rq, @Nullable ResponseJson rp) {
        this.rq = rq;
        this.rp = rp;
    }

    @JsonProperty(value="rq")
    public Integer getRq() {
        return this.rq;
    }

    public void setRq(Integer rq) {
        this.rq = rq;
    }

    @JsonProperty(value="rp")
    public ResponseJson getRp() {
        return this.rp;
    }

    public void setRp(ResponseJson rp) {
        this.rp = rp;
    }

    public String toString() {
        return new Gson().toJson((Object)this);
    }

}
