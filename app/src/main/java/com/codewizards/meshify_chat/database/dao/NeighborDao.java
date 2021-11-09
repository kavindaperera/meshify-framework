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

    @Update(onConflict = OnConflictStrategy.IGNORE)
    void update(Neighbor neighbor);

    @Query("UPDATE neighbor_table SET isNearby = 0 WHERE neighborUuid=:userId")
    void updateNearby(String userId);

    @Query("DELETE FROM neighbor_table")
    void deleteAll();

    @Delete
    void delete(Neighbor neighbor);

    @Query("SELECT * FROM neighbor_table ORDER BY neighborName ASC")
    LiveData<List<Neighbor>> getAlphabetizedNeighbors();

}
