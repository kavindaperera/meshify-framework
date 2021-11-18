package com.codewizards.meshify_chat.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.codewizards.meshify_chat.models.Neighbor;

import java.util.List;

@Dao
public interface NeighborDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Neighbor neighbor);

    @Update
    void update(Neighbor neighbor);

    @Delete
    void delete(Neighbor neighbor);

    @Query("UPDATE neighbor_table SET isNearby = :b WHERE neighborUuid=:userId")
    void updateNearby(String userId, boolean b);

    @Query("UPDATE neighbor_table SET isNearby = 0")
    void updateAllNearby();

    @Query("DELETE FROM neighbor_table")
    void deleteAll();

    @Query("SELECT * FROM neighbor_table ORDER BY isNearby DESC, neighborName ASC")
    LiveData<List<Neighbor>> getAlphabetizedNeighbors();

    @Query("UPDATE neighbor_table SET neighborName = :userName WHERE neighborUuid=:userId")
    void updateNameByUuid(String userId, String userName);
}
