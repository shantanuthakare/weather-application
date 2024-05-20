package com.example.myapplicationwa;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

@Database(entities = {CityEntity.class}, version = 1)
public abstract class DatabaseHelper extends RoomDatabase {

    private static DatabaseHelper instance;

    public abstract CityDao cityDao();

    public static synchronized DatabaseHelper getDB(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            DatabaseHelper.class, "city_db")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
