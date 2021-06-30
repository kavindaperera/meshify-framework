package com.codewizards.meshify.framework.controllers;

import android.content.Context;

import com.codewizards.meshify.client.Config;
import com.codewizards.meshify.client.ConfigProfile;
import com.codewizards.meshify.client.Device;
import com.codewizards.meshify.client.Meshify;
import com.codewizards.meshify.client.Message;
import com.codewizards.meshify.framework.entities.MeshifyEntity;
import com.codewizards.meshify.framework.entities.MeshifyForwardEntity;
import com.codewizards.meshify.framework.expections.MessageException;
import com.codewizards.meshify.logs.Log;

import java.io.IOException;

public class MessageController {

    public final String TAG = "[Meshify][MessageController]";

    private Config config;

    private MessageNotifier messageNotifier;

    private ForwardController forwardController;

    MessageController(Context context, Config config) {
        this.config = config;
        this.messageNotifier = new MessageNotifier(config);
        this.forwardController = new ForwardController();
    }

    void messageReceived(Message message, Session session) {
        message.setMesh(false);
        this.messageNotifier.onMessageReceived(message);
    }

    private void forward(MeshifyForwardEntity forwardEntity) {
        this.forwardController.startForwarding(forwardEntity, true);
    }

    void sendMessage(Context context, Message message, Device device, ConfigProfile profile) {
        Log.d(TAG, "sendMessage: to device -> " + device );

        if (this.config.isEncryption() && !Session.getKeys().containsKey(message.getReceiverId())) {
            Meshify.getInstance().getMeshifyCore().getMessageListener().onMessageFailed(message, new MessageException("Missing public key of " + message.getReceiverId()));
        }

        if (device != null) {
            this.sendMessage(context, message, device);
        } else if (profile != ConfigProfile.NoForwarding) {
            Log.d(TAG, "Device not found. Forwarding the Message....");
            this.forward(new MeshifyForwardEntity(message, profile));
            if (DeviceManager.getDeviceList().isEmpty()) {
                this.messageNotifier.onMessageFailed(message, new MessageException("No Nearby Neighbors found!"));
            }

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
