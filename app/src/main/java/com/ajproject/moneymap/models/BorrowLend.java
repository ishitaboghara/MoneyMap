package com.ajproject.moneymap.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "borrow_lend")
public class BorrowLend {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String personName;
    private double amount;
    private double remainingAmount;
    private String type; // "borrowed" (I owe them) or "lent" (they owe me)
    private String description;
    private long date;
    private boolean isSettled;

    // Constructor
    public BorrowLend(String personName, double amount, double remainingAmount,
                      String type, String description, long date, boolean isSettled) {
        this.personName = personName;
        this.amount = amount;
        this.remainingAmount = remainingAmount;
        this.type = type;
        this.description = description;
        this.date = date;
        this.isSettled = isSettled;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(double remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public boolean isSettled() {
        return isSettled;
    }

    public void setSettled(boolean settled) {
        isSettled = settled;
    }
}