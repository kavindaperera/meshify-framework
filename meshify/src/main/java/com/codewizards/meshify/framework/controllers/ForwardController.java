package com.codewizards.meshify.framework.controllers;

import android.os.AsyncTask;

import com.codewizards.meshify.client.Meshify;
import com.codewizards.meshify.framework.entities.MeshifyEntity;
import com.codewizards.meshify.framework.entities.MeshifyForwardEntity;
import com.codewizards.meshify.framework.entities.MeshifyForwardTransaction;
import com.codewizards.meshify.logs.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class ForwardController {

    public final String TAG = "[Meshify][ForwardController]";

    private ConcurrentNavigableMap<MeshifyForwardEntity, Boolean> concurrentNavigableMap = new ConcurrentSkipListMap<MeshifyForwardEntity, Boolean>();

    ForwardController() {
    }

    void startForwarding(MeshifyForwardEntity forwardEntity, boolean z) {
        synchronized (concurrentNavigableMap) {
            MeshifyForwardEntity forwardEntity1 = forwardEntity;
            if (forwardEntity1 != null) {
                this.concurrentNavigableMap.put(forwardEntity1, true);
            } else {
                //
            }
        }
        if (z) {
            this.sendEntity(SessionManager.getSessions(), false);
        }
    }

    void sendEntity(ArrayList<Session> arrayList, boolean z) {
        new sendEntityToSession(z).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, arrayList.toArray(new Session[arrayList.size()]));
    }

    private List<MeshifyForwardEntity> getMessageList() {
        ArrayList<MeshifyForwardEntity> arrayList = new ArrayList<>();
        for (MeshifyForwardEntity forwardEntity : this.concurrentNavigableMap.descendingKeySet()) {
            arrayList.add(forwardEntity);
        }
        return arrayList;
    }

    void forwardAgain(ArrayList<MeshifyForwardEntity> entityArrayList, Session session) {
        for (MeshifyForwardEntity forwardEntity : entityArrayList) {
//            this.startForwarding(forwardEntity, false);
        }
        this.sendEntity(SessionManager.getSessions(), false);
    }


    private class sendEntityToSession extends AsyncTask<Session, Void, Void> {
        private boolean z = false;

        sendEntityToSession(boolean z){
            this.z = z;
        }

        @Override
        protected Void doInBackground(Session... sessions) {
            if (ForwardController.this.concurrentNavigableMap.size() > 0 || this.z) {
                ArrayList<Session> arrayList = new ArrayList<>();
                for (Session session : sessions) {
                    if (session == null || session.getUserId() == null) continue;
                    arrayList.add(session);
                }
                for (Session session : arrayList) {
                    List list = ForwardController.this.getMessageList();
                    Log.e(TAG, "Sending " + list.size() + " messages to: " + session.getDevice().getDeviceName());
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
