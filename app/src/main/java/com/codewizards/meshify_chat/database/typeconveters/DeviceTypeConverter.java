package com.codewizards.meshify_chat.database.typeconveters;

import androidx.room.TypeConverter;

import com.codewizards.meshify.client.Device;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class DeviceTypeConverter {

    private static Gson gson = new Gson();

    @TypeConverter
    public static Device stringToDevice(String data) {

        Type deviceType = new TypeToken<Device>() {}.getType();

        return gson.fromJson(data, deviceType);
    }

    @TypeConverter
    public static String deviceToString(Device device) {
        return gson.toJson(device);
    }


}
