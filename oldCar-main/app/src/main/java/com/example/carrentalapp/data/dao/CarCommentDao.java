package com.example.carrentalapp.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.carrentalapp.data.entity.CarCommentEntity;
import com.example.carrentalapp.data.model.CarCommentWithUser;

import java.util.List;

@Dao
public interface CarCommentDao {

    @Query("SELECT car_comments.*, COALESCE(users.display_name, users.username, '') AS userDisplayName " +
            "FROM car_comments LEFT JOIN users ON car_comments.user_id = users.id " +
            "WHERE car_comments.car_id = :carId ORDER BY car_comments.created_at DESC")
    List<CarCommentWithUser> loadByCar(long carId);

    @Insert
    long insert(CarCommentEntity entity);

    @Delete
    void delete(CarCommentEntity entity);
}
