package com.example.myapplicationwa;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CityDao {

    @Query("SELECT * FROM city ORDER BY id DESC")
    List<CityEntity> getAllCity();

    @Insert
    void addCity(CityEntity cityEntity);

    @Update
    void updateCity(CityEntity cityEntity);

    @Delete
    void deleteCity(CityEntity cityEntity);
}
