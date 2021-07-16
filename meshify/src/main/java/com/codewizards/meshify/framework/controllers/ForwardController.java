package com.codewizards.meshify.framework.controllers;

import android.os.AsyncTask;

import com.codewizards.meshify.client.Meshify;
import com.codewizards.meshify.framework.entities.MeshifyEntity;
import com.codewizards.meshify.framework.entities.MeshifyForwardEntity;
import com.codewizards.meshify.framework.entities.MeshifyForwardTransaction;
import com.codewizards.meshify.logs.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class ForwardController {

    public final String TAG = "[Meshify][ForwardController]";

    private ConcurrentNavigableMap<MeshifyForwardEntity, Boolean> meshNavigableMap = new ConcurrentSkipListMap<MeshifyForwardEntity, Boolean>();
    private ConcurrentNavigableMap<String, Boolean> reachedNavigableMap = new ConcurrentSkipListMap<String, Boolean>();

    ForwardController() {
    }

    void startForwarding(MeshifyForwardEntity forwardEntity, boolean z) {

        if ( this.reachedNavigableMap.containsKey(forwardEntity.getId())) {
            return;
        }

        synchronized (meshNavigableMap) {
            MeshifyForwardEntity forwardEntity1 = forwardEntity;
            if (forwardEntity1 != null) {
                this.meshNavigableMap.put(forwardEntity1, true);
            } else {
                this.reachedNavigableMap.put(forwardEntity1.getId(), Boolean.TRUE);
            }
        }
        if (z) {
            this.sendEntity(SessionManager.getSessions(), false);
        } 
    }

    MeshifyForwardEntity getEntityFromUuid(String string) {
        for (MeshifyForwardEntity meshifyForwardEntity : this.meshNavigableMap.descendingKeySet()) {
            if (!meshifyForwardEntity.getId().equalsIgnoreCase(string.trim())) continue;
            return meshifyForwardEntity;
        }
        return null;
    }

    private void processReach(MeshifyForwardEntity meshifyForwardEntity, boolean bl) {
        this.meshNavigableMap.remove(meshifyForwardEntity);
        if (bl) {
            this.reachedNavigableMap.put(meshifyForwardEntity.getId(), Boolean.TRUE);
        }
    }

    @SuppressWarnings("deprecation")
    void sendEntity(ArrayList<Session> arrayList, boolean z) {
        new sendEntityToSession(z).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, arrayList.toArray(new Session[arrayList.size()]));
    }


    public void processReach(String reach) {
        this.processReach(this.getEntityFromUuid(reach), true);
    }

    private List<MeshifyForwardEntity> getMessageList() {
        ArrayList<MeshifyForwardEntity> arrayList = new ArrayList<>();
        for (MeshifyForwardEntity forwardEntity : this.meshNavigableMap.descendingKeySet()) {
            arrayList.add(forwardEntity);
        }
        return arrayList;
    }

    void forwardAgain(ArrayList<MeshifyForwardEntity> entityArrayList, Session session) {
        for (MeshifyForwardEntity forwardEntity : entityArrayList) {
            if (this.reachedNavigableMap.containsKey(forwardEntity.getId()) || this.meshNavigableMap.containsKey(forwardEntity)) {
                Log.e(TAG, "forwardAgain: Not forwarding duplicated or already reached entity " + forwardEntity);
            } else {
                this.startForwarding(forwardEntity, false); //adding all message entities without start forwarding
            }
        }
        this.sendEntity(SessionManager.getSessions(), false); //start forwarding all message entities
    }

    public void sendReach(MeshifyForwardEntity forwardEntity) {
        MeshifyEntity<MeshifyForwardTransaction> meshifyEntity = MeshifyEntity.reachMessage(forwardEntity.getId());
        for (Session session : SessionManager.getSessions()) {
            try {
                MeshifyCore.sendEntity(session, meshifyEntity);
            }
            catch (Exception exception) {
                Log.e(TAG, "sendReach: Error sending reach message", exception);
            }
        }
    }



    @SuppressWarnings("deprecation")
    private class sendEntityToSession extends AsyncTask<Session, Void, Void> {
        private boolean z = false;

        sendEntityToSession(boolean z){
            this.z = z;
        }

        @Override
        protected Void doInBackground(Session... sessions) {
            if (ForwardController.this.meshNavigableMap.size() > 0 || this.z) {
                ArrayList<Session> arrayList = new ArrayList<>();
                for (Session session : sessions) {
                    if (session == null || session.getUserId() == null) continue;
                    arrayList.add(session);
                }
                for (Session session : arrayList) {
                    List list = ForwardController.this.getMessageList();
                    Log.d(TAG, "Sending " + list.size() + " messages to: " + session.getDevice().getDeviceName());
                    if (list.size() > 0 || this.z) {
                        MeshifyEntity<MeshifyForwardTransaction> meshifyEntity = MeshifyEntity.meshMessage((ArrayList)list, Meshify.getInstance().getMeshifyClient().getUserUuid());
                        try {
                            MeshifyCore.sendEntity(session, meshifyEntity);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return null;
        }
    }

}
