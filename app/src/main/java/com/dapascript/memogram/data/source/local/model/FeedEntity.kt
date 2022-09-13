package com.dapascript.memogram.data.source.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feed")
data class FeedEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val image: String,
    val date: String,
)
