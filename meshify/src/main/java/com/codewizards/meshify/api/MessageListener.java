package com.codewizards.meshify.api;

import com.codewizards.meshify.framework.expections.MessageException;

/**
 * This class allows to define callback functions for Message listening process
 */
public interface MessageListener {

    /**
     * Callback when a message was received successfully
     *
     * @param message The Message object representing the received message
     */
    void onMessageReceived(Message message);

    /**
     * Callback when a message was not successfully received
     *
     * @param message   The Message object representing the failed message
     * @param exception The MessageException object captured at runtime
     */
    void onMessageFailed(Message message, MessageException exception);

    /**
     * Callback when a broadcast message was successfully received
     *
     * @param message The Message object representing the received message
     */
    void onBroadcastMessageReceived(Message message);

    void onMessageSent(String messageId);
}
