package com.codewizards.meshify.framework.entities;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class MeshifyForwardTransactionSerializer extends JsonSerializer<MeshifyForwardTransaction> {
    @Override
    public void serialize(MeshifyForwardTransaction value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        if (value.sender != null) {
            gen.writeStringField("sender", value.sender);
        }
        if (value.reach != null) {
            gen.writeStringField("reach", value.reach);
        }
        if (value.getMesh() != null) {
            int size = value.getMesh().size();
            if (size == 1) {
                gen.writeObjectField("mesh", (Object)value.getMesh().get(0));
            } else {
                gen.writeArrayFieldStart("mesh");
                for (MeshifyForwardEntity forwardPacket : value.getMesh()) {
                    gen.writeObject((Object)forwardPacket);
                }
                gen.writeEndArray();
            }
        }
        gen.writeEndObject();
    }
}
