package com.ajproject.moneymap.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.ajproject.moneymap.models.GroupExpense;

import java.util.List;

@Dao
public interface GroupExpenseDao {

    @Insert
    void insert(GroupExpense groupExpense);

    @Update
    void update(GroupExpense groupExpense);

    @Delete
    void delete(GroupExpense groupExpense);

    @Query("SELECT * FROM group_expenses ORDER BY date DESC")
    List<GroupExpense> getAllGroupExpenses();

    @Query("SELECT * FROM group_expenses WHERE groupId = :groupId ORDER BY date DESC")
    List<GroupExpense> getExpensesByGroup(int groupId);

    @Query("DELETE FROM group_expenses WHERE groupId = :groupId")
    void deleteByGroupId(int groupId);

    @Query("DELETE FROM group_expenses")
    void deleteAll();
}