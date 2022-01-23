package com.codewizards.meshify.framework.controllers;

import android.os.Handler;
import android.os.Looper;

import com.codewizards.meshify.api.Config;
import com.codewizards.meshify.api.Meshify;
import com.codewizards.meshify.api.Message;
import com.codewizards.meshify.framework.expections.MessageException;

class MessageNotifier {

    MessageNotifier(Config config) {

    }

    void onMessageReceived(Message message) {
        new Handler(Looper.getMainLooper()).post(() -> Meshify.getInstance().getMeshifyCore().getMessageListener().onMessageReceived(message));
    }

    void onBroadcastMessageReceived(Message message) {
        new Handler(Looper.getMainLooper()).post(() -> Meshify.getInstance().getMeshifyCore().getMessageListener().onBroadcastMessageReceived(message));
    }

    void onMessageFailed(Message message, MessageException messageException) {
        new Handler(Looper.getMainLooper()).post(() -> Meshify.getInstance().getMeshifyCore().getMessageListener().onMessageFailed(message, messageException));
    }
}