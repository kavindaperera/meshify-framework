package com.codewizards.meshify_chat.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.codewizards.meshify_chat.models.Neighbor;
import com.codewizards.meshify_chat.repositories.NeighborRepository;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

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

    public void updateNearby(String userId) { mRepository.updateNearby(userId); }

    public void delete(Neighbor neighbor) { mRepository.delete(neighbor); }

}
