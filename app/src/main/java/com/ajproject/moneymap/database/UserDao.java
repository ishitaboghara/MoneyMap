package com.ajproject.moneymap.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.ajproject.moneymap.models.User;

@Dao
public interface UserDao {

    @Insert
    void insert(User user);

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    User login(String email, String password);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);

    @Query("SELECT COUNT(*) FROM users")
    int getUserCount();
}