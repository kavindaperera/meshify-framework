package com.codewizards.meshify.client;

import android.util.Base64;

import com.codewizards.meshify.logs.Log;
import com.google.gson.JsonParseException;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;

import javax.crypto.Cipher;

/**
 * Can be used to generate Public-Private key pairs for RSA,
 * to encrypt data using Public key and decrypt data using Private key
 */
public class MeshifyRSA {

    /**
     * This will generate a Public-Private key pair for RSA and returns them in a HashMap
     * @return a HashMap containing two key-value pairs for public & private keys
     * @throws NoSuchAlgorithmException
     */
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

    /**
     * Encrypts the given byte array using the given Base64 encoded public key and
     * returns the encrypted data as a byte array
     * @param base64PublicKey  a public key encoded in Base64
     * @param bytes  a byte array of the content to be encrypted
     * @return  a byte array of the encrypted data
     */
    public static byte[] encrypt (String base64PublicKey, byte[] bytes){

        byte[] encryptedBytes = null;

        try {

            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.decode(base64PublicKey.getBytes(),0));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(keySpec);

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            encryptedBytes = cipher.doFinal(bytes);

        } catch (Exception ex) {
            Log.d("MeshifyRsA.encrypt",ex.getMessage());
            ex.printStackTrace();
        }
        
        return encryptedBytes;

    }

    /**
     * Decrypts the given byte array of encrypted data using the given Base64 encoded private key and
     * returns the decrypted data as a byte array
     * @param base64PrivateKey  a private key encoded in Base64
     * @param encryptedBytes  a byte array of the encrypted data to be decrypted
     * @return  a byte array of the decrypted data
     */
    public static byte[] decrypt (String base64PrivateKey, byte[] encryptedBytes){

        byte[] decryptedBytes = null;

        try {

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decode(base64PrivateKey.getBytes(),0));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey privateKey = keyFactory.generatePublic(keySpec);

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            decryptedBytes = cipher.doFinal(encryptedBytes);

        } catch (Exception ex) {
            Log.d("MeshifyRsA.decrypt",ex.getMessage());
            ex.printStackTrace();
        }

        return decryptedBytes;

    }


    /**
     * Encodes the given byte array as a Base64 encoded String and returns it
     * @param byteArray a byte array of data
     * @return  a String encoded in Base64
     */
    public static String base64StringFromBytes(byte[] byteArray) { //base64 encode to avoid changes when saving
        return Base64.encodeToString((byte[])byteArray, (int)0);
    }


    /**
     * Given a String encoded in Base64 returns a String decoded from Base64
     * @param base64 a String encoded in Base64
     * @return  a String decoded from Base64
     */
    public static String decodeBase64StringFromString(String base64) { //base64 decode
        byte[] arrby = Base64.decode((String)base64, (int)0);
        return new String(arrby, StandardCharsets.UTF_8);
    }

}
