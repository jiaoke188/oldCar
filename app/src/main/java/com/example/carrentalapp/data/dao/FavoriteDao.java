package com.example.carrentalapp.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.carrentalapp.data.entity.FavoriteEntity;
import com.example.carrentalapp.data.model.FavoriteWithCar;

import java.util.List;

@Dao
public interface FavoriteDao {

    @Query("SELECT favorites.*, cars.name AS carName, cars.brand AS carBrand, cars.daily_price AS carPrice " +
            "FROM favorites INNER JOIN cars ON favorites.car_id = cars.id " +
            "WHERE favorites.user_id = :userId ORDER BY favorites.created_at DESC")
    List<FavoriteWithCar> loadByUser(long userId);

    @Query("SELECT * FROM favorites WHERE user_id = :userId AND car_id = :carId LIMIT 1")
    FavoriteEntity find(long userId, long carId);

    @Insert
    long insert(FavoriteEntity entity);

    @Delete
    void delete(FavoriteEntity entity);

    @Query("DELETE FROM favorites WHERE user_id = :userId AND car_id = :carId")
    void deleteByUserAndCar(long userId, long carId);
}
