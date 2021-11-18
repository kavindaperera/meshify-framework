package com.codewizards.meshify.framework.controllers;

import android.bluetooth.BluetoothSocket;
import android.content.SharedPreferences;
import android.os.Parcel;

import com.codewizards.meshify.client.Config;
import com.codewizards.meshify.client.ConfigProfile;
import com.codewizards.meshify.client.Device;
import com.codewizards.meshify.client.Meshify;
import com.codewizards.meshify.client.Message;
import com.codewizards.meshify.framework.entities.MeshifyContent;
import com.codewizards.meshify.framework.entities.MeshifyEntity;
import com.codewizards.meshify.framework.entities.MeshifyForwardTransaction;
import com.codewizards.meshify.framework.entities.MeshifyHandshake;
import com.codewizards.meshify.framework.entities.ResponseJson;
import com.codewizards.meshify.framework.expections.MessageException;
import com.codewizards.meshify.logs.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class Session extends AbstractSession implements com.codewizards.meshify.client.Session, Comparable<Session> {

    private static final String TAG = "[Meshify][Session]";

    private int state = 0;  // 0 = disconnected | 1 = connecting | 2 = connected

    long createTime; //session start time

    private Timer timer; //schedule background timer

    private String sessionId;

    public Session(BluetoothSocket bluetoothSocket) {
        super(bluetoothSocket);
    }

    public Session() {
    }

    void run() {
        Observable.create(observableEmitter -> {
            this.setCreateTime();
            while (this.isConnected()){
                try {
                    int readInt = this.getDataInputStream().readInt();
                    byte[] arrby = new byte[readInt];
                    this.getDataInputStream().readFully(arrby);
                    observableEmitter.onNext(arrby);
                } catch (IOException ioException) {
                    observableEmitter.tryOnError(ioException);
                }
            }
        }).subscribeOn(Schedulers.newThread()).subscribe((Observer) new Observer<byte[]>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                Log.d(TAG, "run: start session " + Session.this.getSessionId() + " device: " + Session.this.getDevice().getDeviceAddress());
                if (Session.this.getAntennaType() == Config.Antenna.BLUETOOTH) {
                    try {
                        Session.this.initialize(Session.this, Session.this.getBluetoothSocket().getOutputStream(), Session.this.getBluetoothSocket().getInputStream());
                    }
                    catch (IOException iOException) {
                        DeviceManager.removeDevice(Session.this.getDevice());
                    }
                }
            }

            @Override
            public void onNext(@NonNull byte[] bytes) {
                try {
                    Parcel parcel = MeshifyUtils.unmarshall(bytes);
                    MeshifyEntity meshifyEntity = MeshifyEntity.CREATOR.createFromParcel(parcel);
                    Log.d(TAG, "Received: " + meshifyEntity);
                    Session.this.processEntity(meshifyEntity);
                } catch (Exception exception) {
                    Log.e(TAG, "Received: reading entity failed " + exception.getMessage(), exception);
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Log.e(TAG, "run: connection broken: [ " + e.getMessage() + " ]");
                Session.this.removeSession();
            }

            @Override
            public void onComplete() {

            }
        });

    }

    private void setCreateTime() {
        this.createTime = System.currentTimeMillis();
    }

    void removeSession() {
        switch (this.getAntennaType()) {
            case BLUETOOTH: {
                try {
                    if (this.getDataInputStream() != null && this.getDataOutputStream() != null) {
                        this.getDataInputStream().close();
                        this.getDataOutputStream().close();
                    }
                    if (this.getBluetoothSocket() != null) {
                        this.getBluetoothSocket().close();
                    }
                    this.setBluetoothSocket(null);
                }
                catch (Exception exception) {
                    exception.printStackTrace();
                }
                break;
            }
        }

        if (this.timer != null) {
            this.timer.cancel();
            this.timer = null;
        }

        this.setConnected(false);

        Device device = this.getDevice();
        if (device != null) {
            device.setSessionId(null);
        }

        SessionManager.removeQueueSession(this);
    }

    private void initialize(Session session, OutputStream outputStream, InputStream inputStream) {
        this.setDataOutputStream(new DataOutputStream(outputStream));
        this.setDataInputStream(new DataInputStream(inputStream));
        this.requestHandShake(session);
    }

    private MeshifyHandshake processHandshake(MeshifyHandshake meshifyHandshake) {
        ResponseJson responseJson = null;
        Integer rq = -1; // rq = 1 don't reply
        if (meshifyHandshake.getRq() != -1) {
            switch (meshifyHandshake.getRq()) {
                case 0: {
                    Log.i(TAG, "processHandshake: request type 0 device: " + this.getDevice().getDeviceAddress());
                    responseJson = ResponseJson.ResponseTypeGeneral(Meshify.getInstance().getMeshifyClient().getUserUuid());
                    break;
                }
                case 1: {
                    Log.i(TAG, "processHandshake: request type 1 device: " + this.getDevice().getDeviceAddress());
                    Log.i(TAG, "processHandshake: public key requested: " + Meshify.getInstance().getMeshifyClient().getPublicKey());
                    responseJson = ResponseJson.ResponseTypeKey(Meshify.getInstance().getMeshifyClient().getPublicKey());
                    break;
                }
                case 2: {
                    Log.i(TAG, "processHandshake: request type 2 device: " + this.getDevice().getDeviceAddress());
                    Log.i(TAG, "processHandshake: neighbor details requested: ");

                    ArrayList<Session> sessions= SessionManager.getSessions();
                    if (sessions != null) {
                        HashMap<String, Object> neighborDetails = new HashMap<>();
                        for (Session session : sessions) {
                            Device device = session.getDevice();
                            neighborDetails.put(device.getDeviceName(), device);
                            //Message message = new Message(neighborDetails, Meshify.getInstance().getMeshifyClient().getUserUuid(), this.getUserId(), true, 3);
                            //Meshify.getInstance().getMeshifyCore().sendBroadcastMessage(message, ConfigProfile.Default);
                            responseJson = ResponseJson.ResponseTypeNeighborDetails(neighborDetails);
                        }
                    }
                    break;
                }
            }
        }

        if (meshifyHandshake.getRp() != null) {
            switch (meshifyHandshake.getRp().getType()) {
                case 0: {
                    Log.i(TAG, "processHandshake: response type 0 device: " + this.getDevice().getDeviceAddress() );
                    this.setUserId(meshifyHandshake.getRp().getUuid());

                    this.getDevice().setUserId(meshifyHandshake.getRp().getUuid());
                    DeviceManager.addDevice(this.getDevice());

                    // check whether public key already exists
                    HashMap<String,String> publicKeysMap = getKeys();
                    if (!publicKeysMap.containsKey(meshifyHandshake.getRp().getUuid())) {

                        if (Meshify.getInstance().getConfig().isEncryption()) {
                            Log.i(TAG, "processHandshake: asking for key");
                            rq = 1;
                        }
                    }
                    break;
                }
                case 1: {
                    Log.i(TAG, "processHandshake: response type 1 : public key received " + meshifyHandshake.getRp().getKey());
                    this.setPublicKey(meshifyHandshake.getRp().getKey());
                    Session.saveKey(this.getUserId(), meshifyHandshake.getRp().getKey());

                    Log.i(TAG, "processHandshake: asking for neighbor details");
                    rq = 2;
                    break;
                }
                case 2: {
                    Log.i(TAG, "processHandshake: response type 2 : neighbor details received ");
                    HashMap<String, Object> neighborDetails = meshifyHandshake.getRp().getNeighborDetails();
                    Iterator<String> keys = neighborDetails.keySet().iterator();
                    while(keys.hasNext()){
                        String deviceName = keys.next();
                        Log.i(TAG, deviceName + "details received ");
                    }
                }
            }
        }

        return new MeshifyHandshake(rq, responseJson);

    }

    private static SharedPreferences getSharedPreferences() {
        return Meshify.getInstance().getMeshifyCore().getSharedPreferences();
    }

    private static SharedPreferences.Editor getEditor() {
        return Meshify.getInstance().getMeshifyCore().getEditor();
    }

    static HashMap<String, String> getKeys() {
        String string = Session.getSharedPreferences().getString(MeshifyCore.PREFS_KEY_PAIRS, null);
        if (string != null) {
            Type type = new TypeToken<HashMap<String, Object>>(){}.getType();
            return (HashMap) new Gson().fromJson(string, type);
        }
        return new HashMap<String, String>();
    }

    public static void saveKey(String userId, String key) {
        HashMap<String, String> hashMap = Session.getKeys();
        hashMap.put(userId, key);
        String string = new Gson().toJson(hashMap);
        Session.getEditor().putString(MeshifyCore.PREFS_KEY_PAIRS, string).commit();
    }

    void processEntity(MeshifyEntity meshifyEntity) {
        if (meshifyEntity != null) {
            switch (meshifyEntity.getEntity()) {
                case 0:{
                    this.setConnected(true);
                    MeshifyHandshake meshifyHandshake = (MeshifyHandshake) meshifyEntity.getContent();
                    MeshifyHandshake meshifyHandshake2 = this.processHandshake(meshifyHandshake);

                    if (meshifyHandshake2.getRq() != -1 || meshifyHandshake2.getRp() != null) {
                        try {
                            MeshifyCore.sendEntity(this, MeshifyEntity.generateHandShake(meshifyHandshake2)); // send handshake reply
                        }
                        catch (MessageException | IOException exception) {
                            exception.printStackTrace();
                        }
                    }
                    DeviceManager.addDevice(this.getDevice(), this);
                    if (!this.isClient() || this.getEmitter() == null) break;
                    this.getEmitter().onComplete();
                    break;
                }
                case 1: {
                    this.setConnected(true);
                    MeshifyContent content =  (MeshifyContent) meshifyEntity.getContent();
                    Object object;
                    HashMap hashMap = null;
                    if (content.getPayload() != null) {
                        object = new Gson().toJson(content.getPayload());
                        hashMap = (HashMap) new Gson().fromJson((String) object, new TypeToken<HashMap<String, Object>>(){}.getType());
                    }

                    object = new Message.Builder();
                    ((Message.Builder)object).setContent(hashMap);
                    Message message = ((Message.Builder)object).build();
                    message.setReceiverId(Meshify.getInstance().getMeshifyClient().getUserUuid());
                    message.setSenderId(this.getUserId());
                    message.setUuid(content.getId());

                    Meshify.getInstance().getMeshifyCore().getMessageController().messageReceived(message, this);
                    break;
                }
                case 2: {

                    MeshifyForwardTransaction forwardTransaction = (MeshifyForwardTransaction) meshifyEntity.getContent();


                    if (forwardTransaction.getReach() != null || (forwardTransaction.getMesh() != null && forwardTransaction.getMesh().size() == 0) ) {
                        Meshify.getInstance()
                                .getMeshifyCore()
                                .getMessageController()
                                .incomingReachAction(forwardTransaction.getReach());
                        return;
                    }

                    Meshify.getInstance()
                            .getMeshifyCore()
                            .getMessageController()
                            .incomingMeshMessageAction(this, meshifyEntity);

                    break;
                }

            }
        }
    }

    void flush(MeshifyEntity meshifyEntity) throws IOException, MessageException, InterruptedException {
        try {
            Log.d(TAG, "Flushed:" + meshifyEntity);

            byte[] arrby = MeshifyUtils.marshall(meshifyEntity);

            this.getDataOutputStream().writeInt(arrby.length);
            this.getDataOutputStream().write(arrby);
            this.getDataOutputStream().flush();

        }
        catch (IOException | NullPointerException exception) {
            Log.e(TAG, "Output stream or session was null, removing session: " + this.getSessionId(), exception);
            this.removeSession();
        }
    }

    int getState() {
        return this.state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @Override
    public void setConnected(boolean connected) {
        super.setConnected(connected);
        if (connected) {
            this.setState(2);
        } else {
            this.setState(0);
        }
    }


    @Override
    public void disconnect() {
        SessionManager.removeQueueSession(this);
    }

    public boolean equals(Object obj) {
        if (obj instanceof Session) {
            return this.getSessionId().equals(((Session)obj).getSessionId());
        }
        return false;
    }

    @Override
    public int compareTo(Session session) {
        return String.valueOf(this.getSessionId()).compareTo(String.valueOf(session.getSessionId()));
    }

    public String getSessionId() {
        return this.sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    private void requestHandShake(Session session) {
        Log.e(TAG, "isClient?: " + session.isClient() + " requestHandShake: " + session.getSessionId());
        if (session.isClient()) {
            try {
                Log.e(this.TAG, "Handshake request type 0 |  session: " + getSessionId());
                MeshifyEntity meshifyEntity = MeshifyEntity.generateHandShake();

                MeshifyCore.sendEntity(session, meshifyEntity); // send handshake first
            }
            catch (MessageException | IOException exception) {
                exception.printStackTrace();
            }

        }
    }
}
