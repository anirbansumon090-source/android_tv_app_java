package com.example.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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

    /**
     * Custom HTTP request headers needed to play this channel's stream, stored as a
     * JSON object string (e.g. {"Referer":"https://example.com","User-Agent":"..."}).
     * Many protected/anti-hotlink IPTV sources require a specific Referer, Origin,
     * User-Agent, or Cookie/token header or they reject playback with 403.
     * Stored as plain text so no extra Room TypeConverter is needed.
     */
    private String headersJson;

    public Channel(String name, String category, String url, String logoUrl, String description, int number) {
        this.name = name;
        this.category = category;
        this.url = url;
        this.logoUrl = logoUrl;
        this.description = description;
        this.number = number;
        this.headersJson = null;
    }

    @Ignore
    public Channel(String name, String category, String url, String logoUrl, String description,
                   int number, Map<String, String> headers) {
        this(name, category, url, logoUrl, description, number);
        setHeaders(headers);
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

    public String getHeadersJson() { return headersJson; }
    public void setHeadersJson(String headersJson) { this.headersJson = headersJson; }

    /** Parses the stored JSON into a header name -> value map. Never returns null. */
    @Ignore
    public Map<String, String> getHeaders() {
        Map<String, String> map = new HashMap<>();
        if (headersJson == null || headersJson.trim().isEmpty()) return map;
        try {
            JSONObject json = new JSONObject(headersJson);
            Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                map.put(key, json.optString(key));
            }
        } catch (JSONException e) {
            // Malformed headers stored for this channel; ignore and play without extra headers.
        }
        return map;
    }

    /** Replaces the stored headers with the given map (pass null/empty to clear). */
    @Ignore
    public void setHeaders(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            this.headersJson = null;
            return;
        }
        JSONObject json = new JSONObject();
        try {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                json.put(entry.getKey(), entry.getValue());
            }
            this.headersJson = json.toString();
        } catch (JSONException e) {
            this.headersJson = null;
        }
    }
}

