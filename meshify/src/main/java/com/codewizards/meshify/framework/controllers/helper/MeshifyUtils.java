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

    public static ArrayList<byte[]> chunkAndCompress(MeshifyEntity meshifyEntity, int maxLength) throws IOException {
        byte[] byteArr;
        int entityType = meshifyEntity.getEntity();
        Log.e(TAGX, "entityType: " + entityType);
        if (entityType == 0) {
            byteArr = null;
        } else if (entityType == 1) {
            byteArr = null;
        } else if (entityType == 2) {
            byteArr = null;
        }else {
            throw new IllegalArgumentException("Entity type not supported");
        }
        return getByteArrayList(compress(Utils.fromEntityToMsgPack(meshifyEntity)), byteArr, maxLength);
    }

    private static ArrayList<byte[]> getByteArrayList(byte[] byteArr, byte[] byteArr2, int i) throws IOException {
        int length1 = byteArr.length + 2;
        int length2 = byteArr2 != null ? byteArr2.length : 0;
        if (length2 > 0) {
            length1 += length2 + 1;
        }
        Log.e(TAGX, "length1: " + length1 + " | length2: " + length2);
        ArrayList<byte[]> arrayList = new ArrayList<>();
        if (length1 < i) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            if (length2 > 0) {
                byteArrayOutputStream.write(1);
            } else {
                byteArrayOutputStream.write(2);
            }
            byteArrayOutputStream.write(3);
            byteArrayOutputStream.write(byteArr);
            arrayList.add(byteArrayOutputStream.toByteArray());
            byteArrayOutputStream.flush();
            byteArrayOutputStream.close();
            if (length2 > 0) {
                // TODO
            }
        } else {
            // TODO - chunk the entity
        }
        return arrayList;
    }

    public static byte[] compress(byte[] byteArr) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(byteArr.length);
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(stream);
        gzipOutputStream.write(byteArr);
        gzipOutputStream.close();
        byte[] byteArray = stream.toByteArray();
        stream.close();
        return byteArray;
    }

    public static byte[] decompress(byte[] byteArr) throws IOException {
        GZIPInputStream gZIPInputStream;
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArr);
        if (byteArr.length <= MeshifyUtils.DEFAULT_GZIP_BUFFER_SIZE) {
            gZIPInputStream = new GZIPInputStream(byteArrayInputStream);
        } else {
            gZIPInputStream = new GZIPInputStream(byteArrayInputStream, byteArr.length);
        }
        return IOUtils.toByteArray(gZIPInputStream);
    }

    public static byte[] getByteArray(byte[] byteArr) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArr);
        int read = byteArrayInputStream.read();
        int available = byteArrayInputStream.available(); // get byte array length
        byte[] byteArr2 = new byte[available]; // create empty byte array
        byteArrayInputStream.read(byteArr2, 0, available); // add all available data to the array

        // TODO - check read

        return byteArr2;
    }

    public static int getInt(byte[] byteArr) {
        try {
            return byteArr[0];
        } catch (Exception e) {
            return -1;
        }
    }

    public static MeshifyEntity chunkToEntity(ArrayList<byte[]> arrayList, boolean z) {
        MeshifyEntity meshifyEntity;
        Object fromJson;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream byteArrayOutputStream2 = new ByteArrayOutputStream();

        Iterator<byte[]> it = arrayList.iterator();

        while (it.hasNext()) {
            try {
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(it.next());
                int read = byteArrayInputStream.read();
                int available = byteArrayInputStream.available();
                byte[] bArr = new byte[available];
                byteArrayInputStream.read(bArr, 0, available);
                if (read == 4) {
                    byteArrayOutputStream2.write(bArr);
                } else if (read == 3) {
                    byteArrayOutputStream.write(bArr);
                } else {
                    Log.e(TAG, "chunkToEntity: UNDEFINE " + read);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            byte[] decompress = decompress(byteArrayOutputStream.toByteArray());
            if (z) {
                fromJson = Utils.fromMsgPackToEntity(decompress, MeshifyEntity.class);
            } else {
                fromJson = new Gson().fromJson(new String(decompress), MeshifyEntity.class);
            }
            meshifyEntity = (MeshifyEntity) fromJson;
        } catch (Exception e) {
            e.printStackTrace();
            meshifyEntity = null;
        }
        return meshifyEntity;
    }
}
