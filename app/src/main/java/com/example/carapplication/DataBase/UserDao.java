package com.example.carapplication.DataBase;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.carapplication.Modle.User;

@Dao
public interface UserDao {
    @Insert
    long insert(User user);
    
    @Update
    void update(User user);
    
    @Query("SELECT * FROM users WHERE email = :email AND password = :password")
    User login(String email, String password);
    
    @Query("SELECT * FROM users WHERE email = :email")
    User getUserByEmail(String email);
    
    @Query("SELECT * FROM users WHERE id = :id")
    User getUserById(int id);
    @Query("SELECT full_name FROM users WHERE id = :id")
    LiveData<String> getUserNameById(int id);
    @Query("UPDATE users SET profile_image = :imagePath WHERE id = :userId")
    void updateProfileImagePath(int userId, String imagePath);

    @Query("UPDATE users SET profile_image_uri = :imageUri WHERE id = :userId")
    void updateProfileImageUri(int userId, String imageUri);

    @Query("UPDATE users SET profile_image = :imagePath, profile_image_uri = :imageUri WHERE id = :userId")
    void updateProfileImage(int userId, String imagePath, String imageUri);
}