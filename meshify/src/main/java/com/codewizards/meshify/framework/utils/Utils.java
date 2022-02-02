package com.codewizards.meshify.framework.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.msgpack.jackson.dataformat.MessagePackFactory;

import java.io.IOException;
import java.util.UUID;

public class Utils {

    public static String generateSessionId() {
        return UUID.randomUUID().toString().substring(0, 5);
    }
}
