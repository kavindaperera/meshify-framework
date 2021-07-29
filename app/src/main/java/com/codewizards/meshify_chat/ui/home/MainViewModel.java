package com.codewizards.meshify_chat.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.codewizards.meshify_chat.models.Neighbor;
import com.codewizards.meshify_chat.repositories.NeighborRepository;

import java.util.List;

public class MainViewModel extends ViewModel {

    private MutableLiveData<List<Neighbor>> mNeighbors; //Mutable data is subclass of Livedata
    private NeighborRepository mRepo;
    private MutableLiveData<Boolean> isUpdating = new MutableLiveData<>();

    public void init(){
        if (mNeighbors != null){
            return;
        }
        mRepo = NeighborRepository.getInstance();
        mNeighbors = mRepo.getNeighbors();
    }

    public LiveData<List<Neighbor>> getNeighbors() { //Livedata cannot be directly changed, only observe
        return mNeighbors;
    }
}
