package com.codewizards.meshify.framework.controllers.helper;

import android.os.Parcel;
import android.os.Parcelable;

import com.codewizards.meshify.framework.entities.MeshifyContent;
import com.codewizards.meshify.framework.entities.MeshifyEntity;
import com.codewizards.meshify.framework.expections.MessageException;
import com.codewizards.meshify.framework.utils.Utils;
import com.codewizards.meshify.logs.Log;
import com.google.gson.Gson;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

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
    private static final String TAG_SESSION = "[Meshify][SessionUtils]";

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

        byte[] byteArr2;
        int entityType = meshifyEntity.getEntity();

        ContentInfo contentInfo;
        ContentInfoUtil contentInfoUtil;

        Log.e(TAG_SESSION, "entityType: " + entityType);
        if (entityType == 0) {
            byteArr2 = null;
        } else if (entityType == 1) {
            byteArr2 = null;

            ArrayList<byte[]> arrayList = new ArrayList<byte[]>();

            if (meshifyEntity.getData() != null && meshifyEntity.getData().length > 0) {
                contentInfoUtil = new ContentInfoUtil();
                contentInfo = contentInfoUtil.findMatch(meshifyEntity.getData());
                Log.i(TAG_SESSION, "chunkAndCompress: match data " + contentInfo.getMimeType());
                Log.i(TAG_SESSION, "chunkAndCompress: match data mime " + contentInfo.getContentType().getMimeType());
                Log.i(TAG_SESSION, "chunkAndCompress: match data simple " + contentInfo.getContentType().getSimpleName());

                if (!(contentInfo == null || contentInfo.getContentType().getMimeType().equals("image/jpeg") || contentInfo.getContentType().getMimeType().equals("image/png") || contentInfo.getContentType().getMimeType().equals("image/gif") )) {
                    arrayList.add((byte[])MeshifyUtils.compress(meshifyEntity.getData()));
                } else {
                    arrayList.add(meshifyEntity.getData());
                }
                byteArr2 = Utils.encodeBinaryBuffer(arrayList);
            }

        } else if (entityType == 2) {
            byteArr2 = null;
        }else {
            throw new IllegalArgumentException("Entity type not supported");
        }
        return getByteArrayList(compress(Utils.fromEntityToMsgPack(meshifyEntity)), byteArr2, maxLength);
    }

    private static ArrayList<byte[]> getByteArrayList(byte[] byteArr, byte[] byteArr2, int maxSize) throws IOException {
        int length1 = byteArr.length + 2;
        int length2 = byteArr2 != null ? byteArr2.length : 0;
        if (length2 > 0) {
            length1 += length2 + 1;
        }
        Log.e(TAG_SESSION, "length1: " + length1 + " | length2: " + length2);
        ArrayList<byte[]> arrayList = new ArrayList<>();

        if (length1 < maxSize) { // check for max size
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
                ByteArrayOutputStream byteArrayOutputStream2 = new ByteArrayOutputStream();
                byteArrayOutputStream2.write(2);
                byteArrayOutputStream2.write(4);
                byteArrayOutputStream2.write(byteArr2);
                arrayList.add(byteArrayOutputStream2.toByteArray());
                byteArrayOutputStream2.flush();
                byteArrayOutputStream2.close();
            }
        } else {


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

        Log.e(TAG_SESSION, "getByteArray(): " + read);

        if (read != 3 && read != 4) {
            return byteArr2;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            byteArrayOutputStream.write(read);
            byteArrayOutputStream.write(byteArr2);
        } catch (IOException e) {
            e.printStackTrace();
        }



        return byteArrayOutputStream.toByteArray();
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
                byte[] arrby = new byte[available];
                byteArrayInputStream.read(arrby, 0, available);

                if (read == 4) {
                    byteArrayOutputStream2.write(arrby);
                } else if (read == 3) {
                    byteArrayOutputStream.write(arrby);
                } else {
                    Log.e(TAG_SESSION, "chunkToEntity: UNDEFINE " + read);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            byte[] decompress = decompress(byteArrayOutputStream.toByteArray());

            byte[] arrby4 = byteArrayOutputStream2.toByteArray();
            if (z) {
                fromJson = Utils.fromMsgPackToEntity(decompress, MeshifyEntity.class);
            } else {
                fromJson = new Gson().fromJson(new String(decompress), MeshifyEntity.class);
            }
            meshifyEntity = (MeshifyEntity) fromJson;

            Log.i(TAG_SESSION, "chunkToEntity: arrby4 " + arrby4.length);

            if (meshifyEntity.getEntity() == 1) {

                if (arrby4 != null && arrby4.length > 0) {

                    ArrayList<byte[]> arrayList1 = Utils.decodeBinaryBuffer(arrby4);

                    MeshifyContent meshifyContent = (MeshifyContent) meshifyEntity.getContent();

                    meshifyEntity.setContent(new MeshifyContent(Utils.fromMsgPackToEntity(decompress(arrayList1.get(0)), HashMap.class), meshifyContent.getId()));

                    if (arrayList1.size() > 0){
                        ContentInfoUtil contentInfoUtil = new ContentInfoUtil();
                        ContentInfo contentInfo = contentInfoUtil.findMatch(arrayList1.get(0));
                        Log.i(TAG_SESSION, "chunkToEntity: match data " + contentInfo.getMimeType());
                        Log.i(TAG_SESSION, "chunkToEntity: match data mime " + contentInfo.getContentType().getMimeType());
                        Log.i(TAG_SESSION, "chunkToEntity: match data simple " + contentInfo.getContentType().getSimpleName());
                        if (!(contentInfo == null || contentInfo.getContentType().getMimeType().equals("image/jpeg") || contentInfo.getContentType().getMimeType().equals("image/png") || contentInfo.getContentType().getMimeType().equals("image/gif"))) {
                            (meshifyEntity).setData(MeshifyUtils.decompress(arrayList1.get(0)));
                        } else {
                            (meshifyEntity).setData(arrayList1.get(0));
                        }
                    }
                }
            } else {
                (meshifyEntity).setBinaryContent(arrby4);
            }

            byteArrayOutputStream.flush();
            byteArrayOutputStream2.flush();

        } catch (Exception e) {
            e.printStackTrace();
            meshifyEntity = null;
            try {
                byteArrayOutputStream.flush();
                byteArrayOutputStream2.flush();
            }
            catch (IOException iOException) {
                iOException.printStackTrace();
            }
        }
        return meshifyEntity;
    }

}
