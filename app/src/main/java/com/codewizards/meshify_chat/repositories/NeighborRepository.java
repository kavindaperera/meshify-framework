package com.codewizards.meshify_chat.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.codewizards.meshify_chat.database.MeshifyRoomDatabase;
import com.codewizards.meshify_chat.database.dao.NeighborDao;
import com.codewizards.meshify_chat.models.Neighbor;

import java.util.List;

public class NeighborRepository {
    private NeighborDao mNeighborDao;
    private LiveData<List<Neighbor>> mAllNeighbors;

    public NeighborRepository(Application application) {
        MeshifyRoomDatabase db = MeshifyRoomDatabase.getDatabase(application);
        mNeighborDao = db.neighborDao();
        mAllNeighbors = mNeighborDao.getAlphabetizedNeighbors();

    }

    public LiveData<List<Neighbor>> getAllNeighbors() {
        return mAllNeighbors;
    }

    public void insert(Neighbor neighbor) {
        MeshifyRoomDatabase.databaseWriteExecutor.execute(() -> {
            mNeighborDao.insert(neighbor);
        });
    }

    public void update(Neighbor neighbor) {
        MeshifyRoomDatabase.databaseWriteExecutor.execute(() -> {
            mNeighborDao.update(neighbor);
        });
    }

    public void delete(Neighbor neighbor) {
        MeshifyRoomDatabase.databaseWriteExecutor.execute(() -> {
            mNeighborDao.delete(neighbor);
        });
    }

    public void updateNearby(String userId, boolean b) {
        MeshifyRoomDatabase.databaseWriteExecutor.execute(() -> {
            mNeighborDao.updateNearby(userId, b);
        });
    }

    public void updateAllNearby() {
        MeshifyRoomDatabase.databaseWriteExecutor.execute(() -> {
            mNeighborDao.updateAllNearby();
        });
    }

}
