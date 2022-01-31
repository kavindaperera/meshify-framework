package com.codewizards.meshify.framework.controllers.helper;

import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Parcel;
import android.os.Parcelable;

import com.codewizards.meshify.api.Meshify;
import com.codewizards.meshify.framework.controllers.MeshifyCore;
import com.codewizards.meshify.framework.entities.MeshifyContent;
import com.codewizards.meshify.framework.entities.MeshifyEntity;
import com.codewizards.meshify.framework.entities.MeshifyForwardEntity;
import com.codewizards.meshify.framework.entities.MeshifyForwardTransaction;
import com.codewizards.meshify.framework.expections.MessageException;
import com.codewizards.meshify.framework.utils.Utils;
import com.codewizards.meshify.logs.Log;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class MeshifyUtils {

    private static final String TAG = "[Meshify][MeshifyUtils]";
    private static final String TAGX = "[Meshify][SessionUtils]";

    private static final Integer DEFAULT_GZIP_BUFFER_SIZE = 512;


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
