package com.codewizards.meshify_chat.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.codewizards.meshify.logs.Log;
import com.codewizards.meshify_chat.database.MeshifyRoomDatabase;
import com.codewizards.meshify_chat.models.Neighbor;
import com.codewizards.meshify_chat.repositories.NeighborRepository;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private static final String TAG = "[Meshify][MainViewModel]" ;

    private NeighborRepository mRepository;
    private LiveData<List<Neighbor>> mAllNeighbors;

    public MainViewModel(@NonNull Application application) {
        super(application);
        if (mAllNeighbors != null){
            return;
        }
        mRepository = new NeighborRepository(application);
        mAllNeighbors = mRepository.getAllNeighbors();
    }

    public LiveData<List<Neighbor>> getAllNeighbors() {
        return mAllNeighbors;
    }

    public void insert(Neighbor neighbor) { mRepository.insert(neighbor); }

    public void update(Neighbor neighbor) { mRepository.update(neighbor); }

    public void delete(Neighbor neighbor) { mRepository.delete(neighbor); }

    public void updateNameByUuid(String userId, String userName) {
        mRepository.updateNameByUuid(userId, userName);
    }

    public void updateNearby(String userId, boolean b) { mRepository.updateNearby(userId, b); }

    public void updateLastSeen(String userId, String lastSeen) { mRepository.updateLastSeen(userId, lastSeen); }

    @Override
    protected void onCleared() {
        Log.e(TAG, "onCleared");
        super.onCleared();
        mRepository.updateAllNearby();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

}
