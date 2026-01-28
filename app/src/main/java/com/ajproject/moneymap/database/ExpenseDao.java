package com.ajproject.moneymap.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.ajproject.moneymap.models.Expense;

import java.util.List;

@Dao
public interface ExpenseDao {

    @Insert
    void insert(Expense expense);

    @Update
    void update(Expense expense);

    @Delete
    void delete(Expense expense);

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    List<Expense> getAllExpenses();

    @Query("SELECT * FROM expenses WHERE type = :type ORDER BY date DESC")
    List<Expense> getExpensesByType(String type);

    @Query("SELECT * FROM expenses WHERE category = :category ORDER BY date DESC")
    List<Expense> getExpensesByCategory(String category);

    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    List<Expense> getExpensesByDateRange(long startDate, long endDate);

    @Query("SELECT SUM(amount) FROM expenses WHERE type = 'expense'")
    double getTotalExpenses();

    @Query("SELECT SUM(amount) FROM expenses WHERE type = 'income'")
    double getTotalIncome();

    @Query("SELECT SUM(amount) FROM expenses WHERE category = :category AND type = 'expense'")
    double getTotalByCategory(String category);

    @Query("DELETE FROM expenses")
    void deleteAll();
}