package com.ajproject.moneymap.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.ajproject.moneymap.models.Group;

import java.util.List;

@Dao
public interface GroupDao {

    @Insert
    long insert(Group group);

    @Update
    void update(Group group);

    @Delete
    void delete(Group group);

    @Query("SELECT * FROM groups ORDER BY createdDate DESC")
    List<Group> getAllGroups();

    @Query("SELECT * FROM groups WHERE id = :groupId")
    Group getGroupById(int groupId);

    @Query("DELETE FROM groups")
    void deleteAll();
}