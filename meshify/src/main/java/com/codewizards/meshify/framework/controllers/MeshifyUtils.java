package com.codewizards.meshify.framework.controllers;

import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

import com.codewizards.meshify.client.Meshify;
import com.codewizards.meshify.client.MeshifyRSA;

import java.util.HashMap;

public class MeshifyUtils {

    /**
     * Converts the given Parcelable object to an array of bytes
     * @param parcelable  a parcelable object
     * @return  the raw bytes of the given Parcelable object
     */
    public static byte[] marshall(Parcelable parcelable, String userId) {
        Parcel parcel = Parcel.obtain();
        parcelable.writeToParcel(parcel, 0);
        byte[] parcelableBytes = parcel.marshall();
        parcel.recycle();

        if (Meshify.getInstance().getConfig().isEncryption()) {
            HashMap<String, String> publicKeysMap = Session.getKeys();
            byte[] encryptedBytes = MeshifyRSA.encrypt(publicKeysMap.get(userId), parcelableBytes);
            return encryptedBytes;
        } else {
            return parcelableBytes;
        }
    }

    public static Parcel unmarshall(byte[] bytes) {
        Parcel parcel = Parcel.obtain();

        if (Meshify.getInstance().getConfig().isEncryption()) {

            SharedPreferences sharedPreferences = Meshify.getInstance().getContext().getSharedPreferences(MeshifyCore.PREFS_NAME, 0);
            String secretKey = sharedPreferences.getString(MeshifyCore.PREFS_PRIVATE_KEY, (String) null);
            if (secretKey != null) {
                byte[] decryptedBytes = MeshifyRSA.decrypt(secretKey, bytes);
                parcel.unmarshall(decryptedBytes, 0, decryptedBytes.length);
            }
        } else {
            parcel.unmarshall(bytes, 0, bytes.length);
        }

        parcel.setDataPosition(0);
        return parcel;
    }

    public static <T> T unmarshall(byte[] bytes, Parcelable.Creator<T> creator) {
        Parcel parcel = unmarshall(bytes);
        T result = creator.createFromParcel(parcel);
        parcel.recycle();
        return result;
    }

}
