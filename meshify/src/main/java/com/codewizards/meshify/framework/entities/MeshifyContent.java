package com.codewizards.meshify.framework.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;

import java.util.HashMap;

public class MeshifyContent{
    @JsonProperty(value="payload")
    private HashMap<String, Object> payload;
    @JsonProperty(value="id")
    private String id;

    public MeshifyContent() {
    }

    public MeshifyContent(String id) {
        this.id = id;
    }

    public MeshifyContent(HashMap<String, Object> payload, String id) {
        this.payload = payload;
        this.id = id;
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
