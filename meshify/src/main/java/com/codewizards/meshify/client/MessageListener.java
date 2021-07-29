package com.codewizards.meshify.client;

import com.codewizards.meshify.framework.expections.MessageException;

/**
 * This class allows to define callback functions for Message listening process
 */
public abstract class MessageListener {

    /**
     * Callback when a message was received successfully
     * @param message The Message object representing the received message
     */
    public void onMessageReceived(Message message) {
    }

    /**
     * Callback when a message was not successfully received
     * @param message The Message object representing the failed message
     * @param exception The MessageException object captured at runtime
     */
    public void onMessageFailed(Message message, MessageException exception) {
    }

    /**
     * Callback when a broadcast message was successfully received
     * @param message The Message object representing the received message
     */
    public void onBroadcastMessageReceived(Message message) {
    }

}
