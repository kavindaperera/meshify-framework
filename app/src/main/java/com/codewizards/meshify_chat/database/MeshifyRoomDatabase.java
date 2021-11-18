package com.codewizards.meshify_chat.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.codewizards.meshify_chat.database.dao.NeighborDao;
import com.codewizards.meshify_chat.models.Neighbor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Neighbor.class}, version = 1)
public abstract class MeshifyRoomDatabase extends RoomDatabase {                //  must be abstract and extend RoomDatabase

    public abstract NeighborDao neighborDao();
    private static volatile MeshifyRoomDatabase INSTANCE;

    @VisibleForTesting
    public static final String DATABASE_NAME = "meshify_database";

    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static MeshifyRoomDatabase getDatabase(final Context context) {

        if (INSTANCE == null) {
            synchronized (MeshifyRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            MeshifyRoomDatabase.class, DATABASE_NAME)
                            // Wipes and rebuilds instead of migrating
                            // if no Migration object.
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
        }

        @Override
        public void onDestructiveMigration(@NonNull SupportSQLiteDatabase db) {
            super.onDestructiveMigration(db);
        }

        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            // If you want to keep data through app restarts,
            // comment out the following block
            databaseWriteExecutor.execute(() -> {
                // Populate the database in the background.
                // If you want to start with more words, just add them.
                NeighborDao dao = INSTANCE.neighborDao();
                dao.deleteAll();

            });
        }
    };

}
