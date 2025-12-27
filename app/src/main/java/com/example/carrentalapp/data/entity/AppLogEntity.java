package com.example.carrentalapp.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "app_logs")
public class AppLogEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "module")
    private String module;

    @ColumnInfo(name = "level")
    private String level;

    @ColumnInfo(name = "message")
    private String message;

    @ColumnInfo(name = "operator")
    private String operator;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
