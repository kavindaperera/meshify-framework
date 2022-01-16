package com.codewizards.meshify.framework.controllers;

import android.os.Parcel;
import android.os.Parcelable;


public class MeshifyUtils {

    /**
     * Converts the given Parcelable object to an array of bytes
     * @param parcelable  a parcelable object
     * @return  the raw bytes of the given Parcelable object
     */
    public static byte[] marshall(Parcelable parcelable) {
        Parcel parcel = Parcel.obtain();
        parcelable.writeToParcel(parcel, 0);
        byte[] parcelableBytes = parcel.marshall();
        parcel.recycle();
        return parcelableBytes;
    }

    public static Parcel unmarshall(byte[] bytes) {
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, 0, bytes.length);
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
