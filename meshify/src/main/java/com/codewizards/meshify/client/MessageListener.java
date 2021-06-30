package com.codewizards.meshify.client;

import com.codewizards.meshify.framework.expections.MessageException;

public abstract class MessageListener {

    public void onMessageReceived(Message message) {
    }

    public void onMessageFailed(Message message, MessageException exception) {
    }

}
