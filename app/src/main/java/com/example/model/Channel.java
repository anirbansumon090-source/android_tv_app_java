package com.example.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "channels")
public class Channel {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String name;
    private String category;
    private String url;
    private String logoUrl;
    private String description;
    private int number;

    public Channel(String name, String category, String url, String logoUrl, String description, int number) {
        this.name = name;
        this.category = category;
        this.url = url;
        this.logoUrl = logoUrl;
        this.description = description;
        this.number = number;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }
}
