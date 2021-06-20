package com.codewizards.meshify.framework.controllers;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Parcel;

import com.codewizards.meshify.client.Config;
import com.codewizards.meshify.client.Device;
import com.codewizards.meshify.client.Meshify;
import com.codewizards.meshify.client.Message;
import com.codewizards.meshify.framework.entities.MeshifyContent;
import com.codewizards.meshify.framework.entities.MeshifyEntity;
import com.codewizards.meshify.framework.entities.MeshifyHandshake;
import com.codewizards.meshify.framework.entities.ResponseJson;
import com.codewizards.meshify.framework.expections.MessageException;
import com.codewizards.meshify.framework.utils.Utils;
import com.codewizards.meshify.logs.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;

import io.reactivex.CompletableEmitter;
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

    private long crc; //Cyclic Redundancy Check - not added

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
                    int n2 = this.getDataInputStream().readInt();
                    byte[] arrby = new byte[n2];
                    this.getDataInputStream().readFully(arrby);
                    observableEmitter.onNext((Object) arrby);
                } catch (IOException ioException) {
                    observableEmitter.tryOnError((Throwable) ioException);
                }
            }
        }).subscribeOn(Schedulers.newThread()).subscribe((Observer)  new Observer<byte[]>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                Log.d(TAG, "run: Start runnable of session " + Session.this.getSessionId() + " device: " + Session.this.getDevice().getDeviceAddress());
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

                Parcel parcel = ChunkUtils.unmarshall(bytes);
                MeshifyEntity meshifyEntity = MeshifyEntity.CREATOR.createFromParcel(parcel);
                Log.e(TAG, "Received -> " + meshifyEntity);
                Session.this.processEntity(meshifyEntity);

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
                    this.setBluetoothSocket((BluetoothSocket)null);
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
                    Log.e(TAG, "processHandshake: request type 0 device: " + this.getDevice().getDeviceAddress());
                    responseJson = ResponseJson.ResponseTypeGeneral(Meshify.getInstance().getMeshifyClient().getUserUuid());
                    break;
                }
                case 1: {
                    Log.e(TAG, "processHandshake: request type 1 device: " + this.getDevice().getDeviceAddress());
                    Log.e(TAG, "processHandshake: public key requested: " + Meshify.getInstance().getMeshifyClient().getPublicKey());
                    responseJson = ResponseJson.ResponseTypeKey(Meshify.getInstance().getMeshifyClient().getPublicKey());
                    break;
                }
            }
        }
        if (meshifyHandshake.getRp() != null) {
            switch (meshifyHandshake.getRp().getType()) {
                case 0: {
                    Log.e(TAG, "processHandshake: response type 0 device: " + this.getDevice().getDeviceAddress() );
                    this.setUserId(meshifyHandshake.getRp().getUuid());

                    this.getDevice().setUserId(meshifyHandshake.getRp().getUuid());
                    DeviceManager.addDevice(this.getDevice());

                    if (Meshify.getInstance().getConfig().isEncryption()) {
                        Log.e(TAG, "processHandshake: response type 1: asking for key" );
                        rq = 1;
                    }
                    break;
                }
                case 1: {
                    Log.e(TAG, "processHandshake: a key received " + meshifyHandshake.getRp().getKey());
                    this.setPublicKey(meshifyHandshake.getRp().getKey());

                }
            }
        }

        return new MeshifyHandshake(rq, responseJson);

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
                            MeshifyCore.sendEntity(this, MeshifyEntity.generateHandShake(meshifyHandshake2));
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
                    MeshifyContent meshifyContent =  (MeshifyContent) meshifyEntity.getContent();

                    if (meshifyContent.getPayload() != null) {
                        Log.e(TAG, "getPayload():" + meshifyContent.getPayload());
                    }
                    Meshify.getInstance().getMeshifyCore().getMessageController().messageReceived(this);
                    break;
                }
            }
        }
    }

    void flush(MeshifyEntity meshifyEntity) throws IOException, MessageException, InterruptedException {
        try {
            Log.e(TAG, "Flushed:" + meshifyEntity);

            byte[] arrby = ChunkUtils.marshall(meshifyEntity);

            this.getDataOutputStream().writeInt(arrby.length);
            this.getDataOutputStream().write(arrby);
            this.getDataOutputStream().flush();

        }
        catch (IOException | NullPointerException exception) {
            Log.e(TAG, "Outputstream or session was null, removing session: " + this.getSessionId(), exception);
            this.removeSession(); //remove the session
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

    public void setCrc(long crc) {
        this.crc = crc;
    }

    @Override
    public long getCrc() {
        return this.crc;
    }

    @Override
    public void disconnect() {

    }

    public boolean equals(Object obj) {
        if (obj instanceof Session) {
            return this.getSessionId().equals(((Session)obj).getSessionId());
        }
        return false;
    }

    @Override
    public int compareTo(Session session) {
        return String.valueOf(this.getCrc()).compareTo(String.valueOf(session.getCrc()));
    }

    public String getSessionId() {
        return this.sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    private void requestHandShake(Session session) {
        Log.d(TAG, "client: " + session.isClient() + " requestHandShake: " + session.getSessionId());
        if (session.isClient()) {
            try {
                Log.e(this.TAG, "1 -> Handshake request type general - UUID |  session: " + getSessionId());
                MeshifyEntity meshifyEntity = MeshifyEntity.generateHandShake();

                MeshifyCore.sendEntity(session, meshifyEntity);
            }
            catch (MessageException | IOException exception) {
                exception.printStackTrace();
            }

        }
    }
}
