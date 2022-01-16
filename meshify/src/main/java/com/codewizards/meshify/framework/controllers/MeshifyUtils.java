package com.codewizards.meshify.framework.controllers;

import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Parcel;
import android.os.Parcelable;

import com.codewizards.meshify.framework.entities.MeshifyEntity;
import com.codewizards.meshify.framework.entities.MeshifyForwardEntity;
import com.codewizards.meshify.framework.entities.MeshifyForwardTransaction;
import com.codewizards.meshify.logs.Log;

import java.util.ArrayList;
import java.util.HashMap;

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

    static int method_a(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        byte[] arrby = bluetoothGattCharacteristic.getValue();
        try {
            return arrby[0];
        }
        catch (Exception exception) {
            return -1;
        }
    }

    public static ArrayList<byte[]> getCompressedChunk(MeshifyEntity meshifyEntity) {

        ArrayList<byte[]> arrayList = new ArrayList<byte[]>();

        if (meshifyEntity.getEntity() == 2 ) {

            ArrayList arrayList1 = new ArrayList();
            MeshifyForwardTransaction meshifyEntityContent = (MeshifyForwardTransaction) meshifyEntity.getContent();
            meshifyEntity.setContent(null);

            if (meshifyEntityContent.getReach() == null){

                ArrayList arrayList2 = new ArrayList();

                if (arrayList2.size() > 0) {

                    meshifyEntityContent.getMesh().removeAll(arrayList2);

                }

            }

        }
        return arrayList;
    }

}
