package com.example.carrentalapp.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.carrentalapp.data.entity.AppLogEntity;

import java.util.List;

@Dao
public interface AppLogDao {

    @Query("SELECT * FROM app_logs ORDER BY created_at DESC")
    List<AppLogEntity> loadAll();

    @Query("SELECT * FROM app_logs WHERE module LIKE '%' || :keyword || '%' OR message LIKE '%' || :keyword || '%' ORDER BY created_at DESC")
    List<AppLogEntity> search(String keyword);

    @Insert
    long insert(AppLogEntity entity);

    @Query("DELETE FROM app_logs")
    void clear();
}
