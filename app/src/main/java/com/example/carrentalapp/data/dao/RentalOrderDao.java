package com.example.carrentalapp.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.carrentalapp.data.entity.RentalOrderEntity;
import com.example.carrentalapp.data.model.OrderWithDetail;

import java.util.List;

@Dao
public interface RentalOrderDao {

    @Query("SELECT rental_orders.*, cars.name AS carName, users.username AS userName " +
            "FROM rental_orders INNER JOIN cars ON rental_orders.car_id = cars.id " +
            "INNER JOIN users ON rental_orders.user_id = users.id " +
            "ORDER BY rental_orders.created_at DESC")
    List<OrderWithDetail> loadAllWithDetail();

    @Query("SELECT rental_orders.*, cars.name AS carName, users.username AS userName " +
            "FROM rental_orders INNER JOIN cars ON rental_orders.car_id = cars.id " +
            "INNER JOIN users ON rental_orders.user_id = users.id " +
            "WHERE rental_orders.order_code LIKE '%' || :keyword || '%' OR cars.name LIKE '%' || :keyword || '%' " +
            "OR users.username LIKE '%' || :keyword || '%' ORDER BY rental_orders.created_at DESC")
    List<OrderWithDetail> searchWithDetail(String keyword);

    @Query("SELECT * FROM rental_orders WHERE id = :id LIMIT 1")
    RentalOrderEntity findById(long id);

    @Insert
    long insert(RentalOrderEntity entity);

    @Update
    void update(RentalOrderEntity entity);

    @Delete
    void delete(RentalOrderEntity entity);
}
