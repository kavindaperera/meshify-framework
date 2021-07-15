package com.codewizards.meshify.framework.controllers;

import android.os.Parcel;
import android.os.Parcelable;

import com.codewizards.meshify.client.Meshify;
import com.codewizards.meshify.client.MeshifyRSA;
import com.codewizards.meshify.framework.entities.MeshifyContent;
import com.codewizards.meshify.framework.entities.MeshifyEntity;

import java.util.HashMap;

public class MeshifyUtils {

    /**
     * Converts the given Parcelable object to an array of bytes
     * @param parcelable  a parcelable object
     * @return  the raw bytes of the given Parcelable object
     */
    public static byte[] marshall(Parcelable parcelable, String userId, int entity) {
        // checking whether not MeshifyHandshake
        boolean willEncrypt = (entity != 0);

        Parcel parcel = Parcel.obtain();
        parcelable.writeToParcel(parcel, 0);
        byte[] parcelableBytes = parcel.marshall();
        parcel.recycle();

        if (Meshify.getInstance().getConfig().isEncryption() && willEncrypt) {
            while (Session.getKeys().isEmpty());
            HashMap<String, String> publicKeysMap = Session.getKeys();
            byte[] encryptedBytes = MeshifyRSA.encrypt(publicKeysMap.get(userId), parcelableBytes);
            // adding one byte (1) in front to show that this is encrypted
            byte[] sendingBytes = new byte[encryptedBytes.length + 1];
            sendingBytes[0] = 1;
            System.arraycopy(encryptedBytes, 0, sendingBytes, 1, encryptedBytes.length);

            return sendingBytes;

        } else {
            // adding one byte (0) in front to show that this is not encrypted
            byte[] sendingBytes = new byte[parcelableBytes.length + 1];
            sendingBytes[0] = 0;
            System.arraycopy(parcelableBytes, 0, sendingBytes, 1, parcelableBytes.length);

            return sendingBytes;
        }
    }

    public static Parcel unmarshall(byte[] receivedBytes) {

        boolean isEncrypted = (receivedBytes[0] == 1);
        Parcel parcel = Parcel.obtain();
        // removing first byte and getting the data
        byte[] requiredBytes = new byte[receivedBytes.length - 1];
        System.arraycopy(receivedBytes, 1, requiredBytes, 0, receivedBytes.length - 1);

        if (isEncrypted) {
            String secretKey = Meshify.getInstance().getMeshifyClient().getSecretKey();
            if (secretKey != null) {
                byte[] decryptedBytes = MeshifyRSA.decrypt(secretKey, requiredBytes);
                parcel.unmarshall(decryptedBytes, 0, decryptedBytes.length);
            }
        } else {
            parcel.unmarshall(requiredBytes, 0, requiredBytes.length);
        }
        parcel.setDataPosition(0); // This is extremely important!
        return parcel;
    }

    public static <T> T unmarshall(byte[] bytes, Parcelable.Creator<T> creator) {
        Parcel parcel = unmarshall(bytes);
        T result = creator.createFromParcel(parcel);
        parcel.recycle();
        return result;
    }

}
