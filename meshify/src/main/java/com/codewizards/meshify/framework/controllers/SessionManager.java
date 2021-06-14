package com.codewizards.meshify.framework.controllers;

import com.codewizards.meshify.client.Config;
import com.codewizards.meshify.logs.Log;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SessionManager {
    private static final String TAG = "[Meshify][SessionManager]";

    private static final int availableProcessors = Runtime.getRuntime().availableProcessors();

    private static final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(3, 9, 3L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    static ConcurrentSkipListMap<String, Session> sessionMap = new ConcurrentSkipListMap<>();

    private SessionManager() {
    }

    public static ArrayList<Session> getSessions() {
        return new ArrayList<Session>(sessionMap.values());
    }

    static void queueSession(Session session) {
        if (!sessionMap.containsKey(session.getSessionId())) {
            Log.d(TAG, "queueSession: " + session.getSessionId() + " ::: first time.");
            session.setConnected(true);
            if (session.getAntennaType() != Config.Antenna.BLUETOOTH_LE) {
                session.create();
            }
            sessionMap.put(session.getSessionId(), session);
        }
    }

    public static synchronized Session getSession(String Id) {
        if (Id == null) {
            return null;
        }
        Session session = sessionMap.get(Id);
        if (session != null) {
            return session;
        }
        for (Session session2 : sessionMap.values()) {
            if (session2.getDevice() == null || session2.getUserId() == null || !session2.getUserId().equals(Id) && !session2.getDevice().getDeviceAddress().equalsIgnoreCase(Id)) continue;
            return session2;
        }
        return session;
    }

}
