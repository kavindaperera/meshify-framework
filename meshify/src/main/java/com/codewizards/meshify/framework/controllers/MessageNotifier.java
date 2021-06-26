package com.codewizards.meshify.framework.controllers;

import android.os.Handler;
import android.os.Looper;

import com.codewizards.meshify.client.Config;
import com.codewizards.meshify.client.Meshify;
import com.codewizards.meshify.client.Message;
import com.codewizards.meshify.framework.expections.MessageException;

class MessageNotifier {

    MessageNotifier(Config config) {

    }

    void onMessageReceived(Message message) {
        new Handler(Looper.getMainLooper()).post(() -> Meshify.getInstance().getMeshifyCore().getMessageListener().onMessageReceived(message));
    }

    void onMessageFailed(Message message, MessageException messageException) {
        new Handler(Looper.getMainLooper()).post(() -> Meshify.getInstance().getMeshifyCore().getMessageListener().onMessageFailed(message, messageException));
    }
}