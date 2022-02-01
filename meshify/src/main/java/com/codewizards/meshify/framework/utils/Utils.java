package com.codewizards.meshify.framework.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.msgpack.jackson.dataformat.MessagePackFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.UUID;

public class Utils {

    public static String generateSessionId() {
        return UUID.randomUUID().toString().substring(0, 5);
    }

    public static <T> byte[] fromEntityToMsgPack(T value) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper(new MessagePackFactory());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return objectMapper.writeValueAsBytes(value);
    }

    public static <T> T fromMsgPackToEntity(byte[] bytes, Class<T> valueType) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper(new MessagePackFactory());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        return (T)objectMapper.readValue(bytes, valueType);
    }

    public static byte[] encodeBinaryBuffer(ArrayList<byte[]> arrayList) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Iterator<byte[]> it = arrayList.iterator();
        while (it.hasNext()) {
            byte[] next = it.next();
            try {
                byteArrayOutputStream.write(next);
            } catch (IOException e) {
                return null;
            }
        }
        return byteArrayOutputStream.toByteArray();
    }

    public static ArrayList<byte[]> decodeBinaryBuffer(byte[] arrby) {
        ArrayList<byte[]> arrayList = new ArrayList<byte[]>();
        arrayList.add(arrby);
        return arrayList;
    }

}
