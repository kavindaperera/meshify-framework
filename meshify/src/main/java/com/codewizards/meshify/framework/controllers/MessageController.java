package com.codewizards.meshify.framework.controllers;

import android.content.Context;
import android.os.Parcelable;

import com.codewizards.meshify.client.Config;
import com.codewizards.meshify.client.ConfigProfile;
import com.codewizards.meshify.client.Device;
import com.codewizards.meshify.client.Meshify;
import com.codewizards.meshify.client.Message;
import com.codewizards.meshify.framework.entities.MeshifyEntity;
import com.codewizards.meshify.framework.entities.MeshifyForwardEntity;
import com.codewizards.meshify.framework.entities.MeshifyForwardTransaction;
import com.codewizards.meshify.framework.expections.MessageException;
import com.codewizards.meshify.logs.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    public void messageReceived(Message message, Session session) {
        message.setMesh(false);
        this.messageNotifier.onMessageReceived(message);
    }

    public void incomingMeshMessageAction(Session session, MeshifyEntity meshifyEntity) {
        MeshifyEntity meshifyEntity1 = meshifyEntity;
        MeshifyForwardTransaction forwardTransaction = (MeshifyForwardTransaction) meshifyEntity.getContent();
        List<MeshifyForwardEntity> mesh = forwardTransaction.getMesh();
        if(mesh != null){
            ArrayList<MeshifyForwardEntity> entityArrayList = new ArrayList<MeshifyForwardEntity>();
            for (MeshifyForwardEntity forwardEntity : mesh) {
                forwardEntity.decreaseHops();
                if (forwardEntity.getReceiver().trim().equalsIgnoreCase(Meshify.getInstance().getMeshifyClient().getUserUuid().trim())){
                    Message message = this.getMessageFromForwardEntity(forwardEntity);
                    if (message != null && message.getContent() == null) {
                        //Error Message
                    } else {
                        this.messageNotifier.onMessageReceived(message);
                    }
                    this.forwardController.sendReach(forwardEntity);
                    continue;
                }

                if (forwardEntity != null && forwardEntity.getHops() > 0 && !Meshify.getInstance().getMeshifyClient().getUserUuid().equalsIgnoreCase(forwardEntity.getSender())) {
                    entityArrayList.add(forwardEntity);
                }

                Log.d(TAG, "incomingMeshMessageAction: remaining hops " + forwardEntity.getHops() );
                continue;
            }

            if (!entityArrayList.isEmpty()){
                Log.e(TAG, "incomingMeshMessageAction: forwarding again....");
                this.forwardController.forwardAgain(entityArrayList, session);
            }

        }
    }

    private Message getMessageFromForwardEntity(MeshifyForwardEntity forwardEntity) {
        Message message = new Message(
                forwardEntity.getPayload(),
                forwardEntity.getReceiver(),
                forwardEntity.getSender(),
                true,
                forwardEntity.getHops());

        if (forwardEntity.getId() != null) {
            message.setUuid(forwardEntity.getId());
        }
        return message;
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
            //TODO - Auto connect check
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

    public void incomingReachAction(String reach) {
        Log.e(TAG, "incomingReachAction: reached " + reach );
        this.forwardController.processReach(reach);
    }
}
