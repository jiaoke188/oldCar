package com.example.carrentalapp.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.carrentalapp.data.entity.CarEntity;

import java.util.List;

@Dao
public interface CarDao {

    @Query("SELECT * FROM cars ORDER BY created_at DESC")
    List<CarEntity> loadAll();

    @Query("SELECT * FROM cars WHERE id = :id LIMIT 1")
    CarEntity findById(long id);

    @Query("SELECT * FROM cars WHERE name LIKE '%' || :keyword || '%' OR brand LIKE '%' || :keyword || '%' OR category LIKE '%' || :keyword || '%' ORDER BY created_at DESC")
    List<CarEntity> search(String keyword);

    @Insert
    long insert(CarEntity entity);

    @Update
    void update(CarEntity entity);

    @Delete
    void delete(CarEntity entity);
}
