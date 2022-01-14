package com.codewizards.meshify.framework.controllers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.codewizards.meshify.client.Config;
import com.codewizards.meshify.client.ConfigProfile;
import com.codewizards.meshify.client.Device;
import com.codewizards.meshify.client.Meshify;
import com.codewizards.meshify.client.Message;
import com.codewizards.meshify.framework.entities.MeshifyEntity;
import com.codewizards.meshify.framework.entities.MeshifyForwardEntity;
import com.codewizards.meshify.framework.entities.MeshifyForwardHandshake;
import com.codewizards.meshify.framework.entities.MeshifyForwardTransaction;
import com.codewizards.meshify.framework.expections.MessageException;
import com.codewizards.meshify.logs.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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
                if (forwardEntity.getMeshType() == 0) {
                    if (forwardEntity.getReceiver() != null && forwardEntity.getReceiver().trim().equalsIgnoreCase(Meshify.getInstance().getMeshifyClient().getUserUuid().trim())){ // msg to me

                        Message message = this.getMessageFromForwardEntity(forwardEntity);

                        if (message != null && message.getContent() == null) {
                            //Error Message
                        } else {
                            this.messageNotifier.onMessageReceived(message);
                        }
                        this.forwardController.sendReach(forwardEntity);
                        continue;
                    }

                    Log.d(TAG, "incomingMeshMessageAction: remaining hops " + forwardEntity.getHops() );

                    if (forwardEntity != null && forwardEntity.getHops() > 0 && !Meshify.getInstance().getMeshifyClient().getUserUuid().equalsIgnoreCase(forwardEntity.getSender())) {
                        entityArrayList.add(forwardEntity);
                        continue;
                    }
                }


                if (forwardEntity.getMeshType() != 1) continue;

                Message message = this.getMessageFromForwardEntity(forwardEntity);
                if (forwardEntity != null && forwardEntity.getHops() > 0 && !Meshify.getInstance().getMeshifyClient().getUserUuid().equalsIgnoreCase(forwardEntity.getSender())) {

                    Log.e(TAG, "Broadcasting");

                    if (!this.forwardController.forwardAgain(forwardEntity)) {
                        forwardEntity.setAdded(new Date(System.currentTimeMillis()));
                        this.forwardController.addForwardEntitiesToList(forwardEntity, true); // broadcasting
                    }

                    this.messageNotifier.onBroadcastMessageReceived(message);


                }

            }

            if (!entityArrayList.isEmpty()){

                Log.e(TAG, "incomingMeshMessageAction: forwarding again....");
                this.forwardController.forwardAgain(entityArrayList, session);

            }

        }
    }

    public void incomingForwardHandshakeAction(Session session, MeshifyEntity meshifyEntity) {

        if (!(this.forwardController.checkHandshakeAlreadyForwarded(meshifyEntity.getId()))) {

            this.forwardController.updateAlreadyForwardedHandshakes(meshifyEntity.getId());
            ((MeshifyForwardHandshake) meshifyEntity.getContent()).decreaseHops();
            MeshifyForwardHandshake forwardHandshake = (MeshifyForwardHandshake) meshifyEntity.getContent();

            // add received neighbor details to your device
            ArrayList<Device> neighborDetails = forwardHandshake.getNeighborDetails();
            if (neighborDetails != null && neighborDetails.size() > 0) {
                for (Device indirectDevice : neighborDetails) {
                    //if (!(session.getDevice().getUserId().equalsIgnoreCase(indirectDevice.getUserId())) && !(Meshify.getInstance().getMeshifyClient().getUserUuid().equalsIgnoreCase(indirectDevice.getUserId()))) {
                    if (!(Meshify.getInstance().getMeshifyClient().getUserUuid().equalsIgnoreCase(indirectDevice.getUserId()))) {
                        Log.i(TAG, "incomingForwardHandshakeAction: neighbor details received: " + indirectDevice.getDeviceName());
                        if (Meshify.getInstance().getMeshifyCore().getConnectionListener() == null)
                            continue;

                        new Handler(Looper.getMainLooper()).post(() -> {
                            Meshify.getInstance().getMeshifyCore().getConnectionListener().onIndirectDeviceFound(indirectDevice);
                        });
                    }
                }
            }

            if (forwardHandshake.getHops() > 0 && !Meshify.getInstance().getMeshifyClient().getUserUuid().equalsIgnoreCase(forwardHandshake.getSender())) {
                this.forwardHandshake(meshifyEntity);
            }
        }
    }

    public void forwardHandshake(MeshifyEntity<MeshifyForwardHandshake> meshifyEntity) {
        this.forwardController.sendEntity(meshifyEntity);
    }

    private Message getMessageFromForwardEntity(MeshifyForwardEntity forwardEntity) {
        Message message = new Message(
                forwardEntity.getPayload(),
                forwardEntity.getReceiver(),
                forwardEntity.getSender(),
                true,
                forwardEntity.getHops());

        message.setDateSent(forwardEntity.getCreatedAt());

        if (forwardEntity.getId() != null) {
            message.setUuid(forwardEntity.getId());
        }
        return message;
    }

    private void forward(MeshifyForwardEntity forwardEntity) {
        this.forwardController.addForwardEntitiesToList(forwardEntity, true); // add and send
    }

    void sendMessage(Context context, Message message, Device device, ConfigProfile profile) {
        Log.d(TAG, "sendMessage: to device -> " + device );
        if (this.config.isEncryption() && !Session.getKeys().containsKey(message.getReceiverId())) {
            Meshify.getInstance().getMeshifyCore().getMessageListener().onMessageFailed(message, new MessageException("Missing public key of " + message.getReceiverId()));
        }
        if (device != null) {
            this.sendMessage(context, message, device);
        } else if (profile != ConfigProfile.NoForwarding) {
            Log.d(TAG, "Device not in range. Forwarding the Message....");
            //TODO - Auto connect check
            this.forward(new MeshifyForwardEntity(message,0, profile));

            if (DeviceManager.getDeviceList().isEmpty()) {
                this.messageNotifier.onMessageFailed(message, new MessageException("No Nearby Neighbors found!"));
            }

        } else if (profile == ConfigProfile.NoForwarding) {
            this.messageNotifier.onMessageFailed(message, new MessageException("Change Forward Profile Settings to use Mesh Forwarding"));
        }
    }

    private void sendMessage(Context context, Message message, Device device) {
        Session session = device.getAntennaType() == Config.Antenna.BLUETOOTH || device.getAntennaType() == Config.Antenna.BLUETOOTH_LE ? SessionManager.getSession(device.getDeviceAddress()) : SessionManager.getSession(device.getSessionId());
        if (session == null) {
            session = SessionManager.getSession(message.getReceiverId());
        }
        if (session != null) {
            try {
                MeshifyCore.sendEntity(session, MeshifyEntity.message(message)); // send message
            } catch (MessageException e) {
                e.printStackTrace();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

    }

    void sendMessage(Message message, ConfigProfile profile) {
        this.forwardController.addForwardEntitiesToList(new MeshifyForwardEntity(message,1, profile),true); // add broadcast message and send
    }

    public Config getConfig() {
        return this.config;
    }

    public void incomingReachAction(String reach) {
        Log.e(TAG, "incomingReachAction: reached " + reach );
        this.forwardController.processReach(reach);
    }
}
