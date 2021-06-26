package com.codewizards.meshify.framework.controllers;

import android.content.Context;

import com.codewizards.meshify.client.Config;
import com.codewizards.meshify.client.ConfigProfile;
import com.codewizards.meshify.client.Device;
import com.codewizards.meshify.client.Message;
import com.codewizards.meshify.framework.entities.MeshifyEntity;
import com.codewizards.meshify.framework.expections.MessageException;
import com.codewizards.meshify.logs.Log;

import java.io.IOException;

public class MessageController {

    public final String TAG = "[Meshify][MessageController]";

    private Config config;
    private MessageNotifier messageNotifier;

    MessageController(Context context, Config config) {
        this.config = config;
        this.messageNotifier = new MessageNotifier(config);
    }

    void messageReceived(Message message, Session session) {
        message.setMesh(false);
        this.messageNotifier.onMessageReceived(message);
    }

    void sendMessage(Context context, Message message, Device device, ConfigProfile profile) {
        Log.e(TAG, "sendMessage: to device -> " + device );
        if (device != null) {
            this.sendMessage(context, message, device);
        }

    }

    private void sendMessage(Context context, Message message, Device device) {

        Session session = device.getAntennaType() == Config.Antenna.BLUETOOTH || device.getAntennaType() == Config.Antenna.BLUETOOTH_LE ? SessionManager.getSession(device.getDeviceAddress()) : SessionManager.getSession(device.getSessionId());
        if (session == null) {
            session = SessionManager.getSession(message.getReceiverId());
        }

        if (session != null) {
            try {
                MeshifyCore.sendEntity(session, MeshifyEntity.message(message));
            } catch (MessageException e) {
                e.printStackTrace();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }


    }

    public Config getConfig() {
        return this.config;
    }
}