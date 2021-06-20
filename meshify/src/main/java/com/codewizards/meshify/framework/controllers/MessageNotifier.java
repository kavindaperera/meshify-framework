package com.codewizards.meshify.framework.controllers;

import android.os.Handler;
import android.os.Looper;

import com.codewizards.meshify.client.Config;
import com.codewizards.meshify.client.Meshify;
import com.codewizards.meshify.client.Message;

class MessageNotifier {

    MessageNotifier(Config config) {

    }

    void onMessageReceived(Message message) {
        new Handler(Looper.getMainLooper()).post(() -> Meshify.getInstance().getMeshifyCore().getMessageListener().onMessageReceived(message));
    }
}