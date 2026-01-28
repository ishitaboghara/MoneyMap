package com.ajproject.moneymap.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "expenses")
public class Expense {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private double amount;
    private String category;
    private String paymentMode; // Cash, UPI, Card
    private String description;
    private long date; // timestamp
    private String type; // "expense" or "income"

    // Constructor
    public Expense(double amount, String category, String paymentMode,
                   String description, long date, String type) {
        this.amount = amount;
        this.category = category;
        this.paymentMode = paymentMode;
        this.description = description;
        this.date = date;
        this.type = type;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}