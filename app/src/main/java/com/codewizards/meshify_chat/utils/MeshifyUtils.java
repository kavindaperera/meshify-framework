package com.codewizards.meshify_chat.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Random;

public class MeshifyUtils {

    public static String generateInitials(String str) {
        try {
            if (str.length() > 1) {
                String[] split = str.replace("(", "").replace(")", "").replace("+", "").split(StringUtils.SPACE);
                if (split.length == 1) {
                    String str2 = split[0];
                    return ("" + str2.charAt(0) + str2.charAt(str2.length() - 1)).toUpperCase();
                } else if (split.length < 2) {
                    return "--";
                } else {
                    return ("" + split[0].charAt(0) + split[1].charAt(0)).toUpperCase();
                }
            } else {
                return str.charAt(0) + "";
            }
        } catch (Exception unused) {
            return "--";
        }
    }

    public static String getRandomColor() {
        Random r = new Random();
        StringBuilder sb = new StringBuilder("#");
        while (sb.length() < 7) {
            sb.append(Integer.toHexString(r.nextInt()));
        }
        return sb.toString().substring(0, 7);
    }
}
