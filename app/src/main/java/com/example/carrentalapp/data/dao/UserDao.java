package com.example.carrentalapp.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.carrentalapp.data.entity.UserEntity;

import java.util.List;

@Dao
public interface UserDao {

    @Query("SELECT * FROM users ORDER BY created_at DESC")
    List<UserEntity> loadAll();

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    UserEntity findByUsername(String username);

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    UserEntity findById(long id);

    @Query("SELECT * FROM users WHERE (username LIKE '%' || :keyword || '%' OR display_name LIKE '%' || :keyword || '%' OR phone LIKE '%' || :keyword || '%') ORDER BY created_at DESC")
    List<UserEntity> search(String keyword);

    @Query("SELECT * FROM users WHERE username = :username AND password = :password AND active = 1 LIMIT 1")
    UserEntity login(String username, String password);

    @Insert
    long insert(UserEntity entity);

    @Update
    void update(UserEntity entity);

    @Delete
    void delete(UserEntity entity);

    @Query("SELECT COUNT(*) FROM users")
    int countAll();
}
