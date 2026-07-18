package com.example.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.model.Channel;
import com.example.model.Setting;

import java.util.List;

@Dao
public interface AppDao {
    @Query("SELECT * FROM channels ORDER BY number ASC")
    List<Channel> getAllChannels();

    @Query("SELECT * FROM channels ORDER BY number ASC")
    LiveData<List<Channel>> getAllChannelsLive();

    @Query("SELECT * FROM channels WHERE id = :id LIMIT 1")
    Channel getChannelById(int id);

    @Query("SELECT * FROM channels WHERE number = :number LIMIT 1")
    Channel getChannelByNumber(int number);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertChannel(Channel channel);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllChannels(List<Channel> channels);

    @Update
    void updateChannel(Channel channel);

    @Delete
    void deleteChannel(Channel channel);

    @Query("DELETE FROM channels")
    void deleteAllChannels();

    @Query("SELECT * FROM settings WHERE `key` = :key LIMIT 1")
    Setting getSetting(String key);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSetting(Setting setting);
}
