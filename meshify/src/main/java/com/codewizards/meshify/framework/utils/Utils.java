package com.codewizards.meshify.framework.utils;

import java.util.UUID;

public class Utils {

    public static String generateSessionId() {
        return UUID.randomUUID().toString().substring(0, 5);
    }

}
