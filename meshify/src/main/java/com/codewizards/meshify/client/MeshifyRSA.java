package com.codewizards.meshify.client;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

class MeshifyRSA {

    static HashMap<String, String> generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        String publicKey = MeshifyRSA.base64StringFromBytes(keyPair.getPublic().getEncoded());
        String privateKey = MeshifyRSA.base64StringFromBytes(keyPair.getPrivate().getEncoded());
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("public", publicKey.trim().replaceAll("[\n\r]", ""));
        hashMap.put("secret", privateKey.trim().replaceAll("[\n\r]", ""));
        return hashMap;
    }

    public static byte[] encrypt (String publicKey, byte[] bytes){

        //TODO - encrypt
        
        return null;

    }

    public static byte[] decrypt (String privateKey, byte[] encryptedBytes){

        //TODO - decrypt

        return null;

    }


    public static String base64StringFromBytes(byte[] byteArray) { //base64 encode to avoid changes when saving
        return Base64.encodeToString((byte[])byteArray, (int)0);
    }

    public static String decodeBase64StringFromString(String base64) { //base64 decode
        byte[] arrby = Base64.decode((String)base64, (int)0);
        return new String(arrby, StandardCharsets.UTF_8);
    }


}
