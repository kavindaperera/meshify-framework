package com.codewizards.meshify.framework.controllers;

import android.content.Context;

import com.codewizards.meshify.client.Config;
import com.codewizards.meshify.client.ConfigProfile;
import com.codewizards.meshify.client.Device;
import com.codewizards.meshify.client.Message;
import com.codewizards.meshify.logs.Log;

public class MessageController {

    public final String TAG = "[Meshify][MessageController]";

    private Config config;

    MessageController(Context context, Config config) {

    }

    void sendMessage(Context context, Message message, Device device, ConfigProfile profile) {
        Log.e(TAG, "sendMessage: " + message);
        //TODO
    }

}
