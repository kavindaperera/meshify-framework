package com.codewizards.meshify.framework.entities;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Iterator;

public class MeshifyEntityDeserializer extends JsonDeserializer<MeshifyEntity> {
    @Override
    public MeshifyEntity deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode readTree = jsonParser.getCodec().readTree(jsonParser);
        int et = (Integer) readTree.get("entity").numberValue();
        JsonNode jsonNode = readTree.get("content");
        Iterator iterator = jsonNode.fieldNames();
        switch (et) {
            case 0: {
                return new MeshifyEntity(et, new ObjectMapper().treeToValue((TreeNode)jsonNode, MeshifyHandshake.class));
            }
        }
        return null;
    }
}
