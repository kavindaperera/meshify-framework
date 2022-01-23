package com.codewizards.meshify_chat.service;

import android.util.Log;

import com.codewizards.meshify.api.Config;
import com.codewizards.meshify_chat.models.Neighbor;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

public class ActiveNeighbors {

    public static String TAG = "[Meshify][MeshifyNotifications]";

    private static ActiveNeighbors INSTANCE;

    private final CopyOnWriteArrayList<Neighbor> activeNeighbors = new CopyOnWriteArrayList<>();

    public static ActiveNeighbors getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ActiveNeighbors();
        }
        return INSTANCE;
    }

    public synchronized void addNeighborIfNotExist(Neighbor neighbor, Config.Antenna antenna) {
        if (neighbor == null) {
            Log.e(TAG, "addNeighborIfNotExist: neighbor is null");
            return;
        }
        boolean b = true;
        if (checkExist(neighbor.getUuid(), antenna) != null && (antenna == Config.Antenna.BLUETOOTH || antenna == Config.Antenna.BLUETOOTH_LE)) {
            b = false;
        }
        if (b) {
            addActiveNeighbor(neighbor, antenna);
        }
    }

    private synchronized Neighbor checkExist(String uuid, Config.Antenna antenna) {
        if (antenna != Config.Antenna.BLUETOOTH && antenna != Config.Antenna.BLUETOOTH_LE) {
            return null;
        }
        return isExist(uuid, this.activeNeighbors);
    }

    private Neighbor isExist(String uuid, CopyOnWriteArrayList<Neighbor> arr) {
        if (uuid == null) {
            return null;
        }
        Iterator<Neighbor> it = arr.iterator();
        while (it.hasNext()) {
            Neighbor next = it.next();
            if (next.getUuid() != null && next.getUuid().trim().equalsIgnoreCase(uuid.trim())) {
                return next;
            }
        }
        return null;
    }

    private synchronized void addActiveNeighbor(Neighbor neighbor, Config.Antenna antenna) {
        if (antenna == Config.Antenna.BLUETOOTH || antenna == Config.Antenna.BLUETOOTH_LE) {
            this.activeNeighbors.add(neighbor);
        }
    }

    public boolean isNeighborNearby(String uuid) {
        Iterator<Neighbor> it = this.activeNeighbors.iterator();
        while (it.hasNext()) {
            if (it.next().getUuid().equals(uuid)) {
                return true;
            }
        }
        return false;
    }

    public void removeNeighbor(String uuid, Config.Antenna antenna) {
        Neighbor n1 = checkExist(uuid, antenna);
        if (n1 != null) {
            removeNeighbor(n1, antenna);
        }

    }

    private synchronized void removeNeighbor(Neighbor neighbor, Config.Antenna antenna) {
        if (neighbor != null) {
            if (antenna == Config.Antenna.BLUETOOTH || antenna == Config.Antenna.BLUETOOTH_LE) {
                Log.d(TAG, "Removed neighbor from BT: " + neighbor.getUuid() + ", " + neighbor.getDeviceName());
                this.activeNeighbors.remove(neighbor);
            }
            if (!neighbor.isNearby()) {
                neighborLost(neighbor, antenna);
            } else {
                Log.v(TAG, "Neighbor is still nearby.");
            }
        }
    }

    private void neighborLost(Neighbor neighbor, Config.Antenna antenna) {
        Log.v(TAG, "Broadcasting Lost Neighbor: " + neighbor.getUuid());
    }

}
