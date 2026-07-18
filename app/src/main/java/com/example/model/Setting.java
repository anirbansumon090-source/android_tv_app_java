package com.example.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

@Entity(tableName = "settings")
public class Setting {
    @PrimaryKey
    @NonNull
    private String key = "";
    private String value;

    public Setting() {
    }

    @Ignore
    public Setting(@NonNull String key, String value) {
        this.key = key;
        this.value = value;
    }

    @NonNull
    public String getKey() { return key; }
    public void setKey(@NonNull String key) { this.key = key; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
