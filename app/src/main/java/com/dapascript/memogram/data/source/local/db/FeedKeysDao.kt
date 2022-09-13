package com.dapascript.memogram.data.source.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dapascript.memogram.data.source.local.model.FeedKeys

@Dao
interface FeedKeysDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKeys(feedKeys: List<FeedKeys>)

    @Query("SELECT * FROM feed_keys WHERE id = :feedId")
    suspend fun getKeys(feedId: String): FeedKeys?

    @Query("DELETE FROM feed_keys")
    suspend fun deleteKeys()
}