package com.ajproject.moneymap.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "group_expenses")
public class GroupExpense {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int groupId;
    private String description;
    private double totalAmount;
    private String paidBy; // Person who paid
    private String splitAmong; // Comma-separated member names
    private String payments; // JSON format: {"memberName": amountPaid}
    private long date;

    // Constructor
    public GroupExpense(int groupId, String description, double totalAmount,
                        String paidBy, String splitAmong, String payments, long date) {
        this.groupId = groupId;
        this.description = description;
        this.totalAmount = totalAmount;
        this.paidBy = paidBy;
        this.splitAmong = splitAmong;
        this.payments = payments;
        this.date = date;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaidBy() {
        return paidBy;
    }

    public void setPaidBy(String paidBy) {
        this.paidBy = paidBy;
    }

    public String getSplitAmong() {
        return splitAmong;
    }

    public void setSplitAmong(String splitAmong) {
        this.splitAmong = splitAmong;
    }

    public String getPayments() {
        return payments;
    }

    public void setPayments(String payments) {
        this.payments = payments;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}