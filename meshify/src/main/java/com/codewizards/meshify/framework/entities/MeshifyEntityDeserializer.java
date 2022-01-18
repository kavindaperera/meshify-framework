package com.codewizards.meshify.framework.entities;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Iterator;

public class MeshifyEntityDeserializer extends JsonDeserializer<MeshifyEntity> {
    @Override
    public MeshifyEntity deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        JsonNode readTree = jsonParser.getCodec().readTree(jsonParser);
        int entity = ((Integer) readTree.get("entity").numberValue()).intValue();
        JsonNode jsonNode = readTree.get("content");
        jsonNode.fieldNames();
        switch (entity) {
            case 0: {
                return new MeshifyEntity(entity, new ObjectMapper().treeToValue(jsonNode, MeshifyHandshake.class));
            }
            case 1: {
                return new MeshifyEntity(entity, new ObjectMapper().treeToValue(jsonNode, MeshifyContent.class));
            }
            case 2: {
                return new MeshifyEntity(entity, new ObjectMapper().enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY).treeToValue(jsonNode, MeshifyForwardTransaction.class));
            }
        }
        return null;
    }
}
