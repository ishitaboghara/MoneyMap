package com.ajproject.moneymap.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.ajproject.moneymap.models.BorrowLend;
import com.ajproject.moneymap.models.Expense;
import com.ajproject.moneymap.models.Group;
import com.ajproject.moneymap.models.GroupExpense;
import com.ajproject.moneymap.models.User;

@Database(entities = {Expense.class, BorrowLend.class, Group.class, GroupExpense.class, User.class},
        version = 2,
        exportSchema = false)
public abstract class MoneyMapDatabase extends RoomDatabase {

    private static MoneyMapDatabase instance;

    // Abstract methods to get DAOs
    public abstract ExpenseDao expenseDao();
    public abstract BorrowLendDao borrowLendDao();
    public abstract GroupDao groupDao();
    public abstract GroupExpenseDao groupExpenseDao();
    public abstract UserDao userDao();

    // Singleton pattern
    public static synchronized MoneyMapDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            MoneyMapDatabase.class,
                            "moneymap_database"
                    )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries() // Only for development
                    .build();
        }
        return instance;
    }
}