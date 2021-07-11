package com.codewizards.meshify_chat.repositories;

import androidx.lifecycle.MutableLiveData;

import com.codewizards.meshify_chat.models.Neighbor;

import java.util.ArrayList;
import java.util.List;

public class NeighborRepository {
    private static NeighborRepository instance;
    private ArrayList<Neighbor> dataSet = new ArrayList<>();

    public static NeighborRepository getInstance(){
        if (instance == null) {
            instance = new NeighborRepository();
        }
        return instance;
    }

    public MutableLiveData<List<Neighbor>> getNeighbors(){
        setNeighbors();
        MutableLiveData<List<Neighbor>> data = new MutableLiveData<>();
        data.setValue(dataSet);
        return data;
    }

    private void setNeighbors(){
//        Neighbor n1 = new Neighbor("e44c9042-7a79-44b8-aac6-63b18ba87466","Saved Contact");
//        n1.setNearby(false);
//        n1.setDeviceType(Neighbor.DeviceType.ANDROID);
//        dataSet.add(n1);
//
//        Neighbor n2 = new Neighbor("e44c9042-7a79-44b8-aac6-63b18ba87466","Saved Contact 2");
//        n2.setNearby(false);
//        n2.setDeviceType(Neighbor.DeviceType.ANDROID);
//        dataSet.add(n2);
    }

}
