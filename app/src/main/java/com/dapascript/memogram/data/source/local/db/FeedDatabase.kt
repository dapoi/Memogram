package com.dapascript.memogram.data.source.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dapascript.memogram.data.source.local.model.FeedEntity
import com.dapascript.memogram.data.source.local.model.FeedKeys

@Database(
    entities = [FeedEntity::class, FeedKeys::class],
    version = 1,
    exportSchema = false
)
abstract class FeedDatabase : RoomDatabase() {

    abstract fun feedDao(): FeedDao
    abstract fun feedKeysDao(): FeedKeysDao
}