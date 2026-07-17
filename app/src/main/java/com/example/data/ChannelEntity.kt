package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "channels")
data class ChannelEntity(
    @PrimaryKey val id: Int, // Channel Number (e.g., 101, 102)
    val name: String,
    val category: String,
    val url: String,
    val logoUrl: String = "",
    val description: String = ""
)
