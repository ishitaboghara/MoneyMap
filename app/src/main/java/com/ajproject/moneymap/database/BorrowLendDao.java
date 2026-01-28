package com.ajproject.moneymap.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.ajproject.moneymap.models.BorrowLend;

import java.util.List;

@Dao
public interface BorrowLendDao {

    @Insert
    void insert(BorrowLend borrowLend);

    @Update
    void update(BorrowLend borrowLend);

    @Delete
    void delete(BorrowLend borrowLend);

    @Query("SELECT * FROM borrow_lend ORDER BY date DESC")
    List<BorrowLend> getAllRecords();

    @Query("SELECT * FROM borrow_lend WHERE type = :type AND isSettled = 0 ORDER BY date DESC")
    List<BorrowLend> getUnsettledByType(String type);

    @Query("SELECT * FROM borrow_lend WHERE isSettled = 0 ORDER BY date DESC")
    List<BorrowLend> getAllUnsettled();

    @Query("SELECT SUM(remainingAmount) FROM borrow_lend WHERE type = 'borrowed' AND isSettled = 0")
    double getTotalBorrowed();

    @Query("SELECT SUM(remainingAmount) FROM borrow_lend WHERE type = 'lent' AND isSettled = 0")
    double getTotalLent();

    @Query("DELETE FROM borrow_lend")
    void deleteAll();
}