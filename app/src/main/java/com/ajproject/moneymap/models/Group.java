package com.ajproject.moneymap.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "groups")
public class Group {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String groupName;
    private String members; // Comma-separated names
    private long createdDate;

    // Constructor
    public Group(String groupName, String members, long createdDate) {
        this.groupName = groupName;
        this.members = members;
        this.createdDate = createdDate;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getMembers() {
        return members;
    }

    public void setMembers(String members) {
        this.members = members;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }
}