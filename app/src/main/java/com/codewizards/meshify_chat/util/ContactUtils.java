package com.codewizards.meshify_chat.util;

import android.Manifest;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.Pair;

import androidx.core.content.ContextCompat;

import com.codewizards.meshify_chat.main.MeshifyApp;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.j256.ormlite.field.FieldType;

import java.util.HashMap;

/**
 * <p>This class contains contacts utilities functions.</p>
 *
 * @author meshify
 * @version 1.0
 */
public class ContactUtils {

    public static String TAG = "[Meshify][ContactUtils]";

    /**
     * @param context
     * @return
     */
    public static boolean checkContactPermissions(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == 0;
    }

    /**
     * @param phone
     * @return
     * @throws Exception
     */
    public static String getContactNameFromPhone(String phone) throws Exception {
        String contact = null;
        try {
            Cursor query = MeshifyApp
                    .getInstance()
                    .getBaseContext()
                    .getContentResolver()
                    .query(Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone)),
                            new String[]{"display_name"},
                            (String) null,
                            (String[]) null,
                            (String) null);

            if (query != null) {
                while (query.moveToNext()) {
                    contact = query.getString(query.getColumnIndexOrThrow("display_name"));
                }
                query.close();
            }

            return contact;

        } catch (SecurityException unused) {
            throw new Exception("READ_CONTACTS permission DENIED");
        }
    }

    /**
     * @param phoneNumber
     * @return
     */
    public static String parseNationalNumber(String phoneNumber) {
        String normalizeDigitsOnly = PhoneNumberUtil.normalizeDigitsOnly(phoneNumber);
        if (normalizeDigitsOnly.length() > 10) {
            return normalizeDigitsOnly.substring(normalizeDigitsOnly.length() - 10);
        }
        if (normalizeDigitsOnly.length() < 10) {
            return null;
        }
        return normalizeDigitsOnly;
    }

    /**
     * @param phone
     * @return
     */
    public Pair<String, String> getContactIdAndNameFromPhone(String phone) {
        try {
            Cursor query = MeshifyApp
                    .getInstance()
                    .getBaseContext()
                    .getContentResolver()
                    .query(Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone)),
                            new String[]{FieldType.FOREIGN_ID_FIELD_SUFFIX, "display_name"},
                            (String) null,
                            (String[]) null,
                            (String) null);

            if (query != null) {
                String contactId = null;
                String contactName = null;
                while (query.moveToNext()) {
                    contactId = query.getString(query.getColumnIndexOrThrow(FieldType.FOREIGN_ID_FIELD_SUFFIX));
                    contactName = query.getString(query.getColumnIndexOrThrow("display_name"));
                }
                query.close();
                return new Pair<>(contactId, contactName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Method allowing to query all phone contacts into a HashMap.
     * @param phone
     * @return a HashMap with all phone contacts
     */
    public HashMap<String, Pair<String, String>> getPhonesNamesAndLabels(String phone) {
        String parseNationalNumber = phone != null ? parseNationalNumber(phone) : "";
        HashMap<String, Pair<String, String>> hashMap = new HashMap<>();
        Cursor query = MeshifyApp
                .getInstance()
                .getBaseContext()
                .getContentResolver()
                .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]{"data1", "display_name", "data2"}, (String) null, (String[]) null, (String) null);
        int columnIndex = query.getColumnIndex("data1");
        int columnIndex2 = query.getColumnIndex("display_name");
        int columnIndex3 = query.getColumnIndex("data2");
        if (query.getCount() >= 1) {
            while (query.moveToNext()) {
                String parseNationalNumber2 = parseNationalNumber(query.getString(columnIndex));
                if (parseNationalNumber2 != null && !parseNationalNumber2.equals(parseNationalNumber)) {
                    hashMap.put(parseNationalNumber2, new Pair(query.getString(columnIndex2), (String) ContactsContract.CommonDataKinds.Phone.getTypeLabel(MeshifyApp.getInstance().getResources(), query.getInt(columnIndex3), "")));
                }
            }
        } else {
            Log.w(TAG, "No contact records found");
        }
        query.close();
        return hashMap;
    }

}
