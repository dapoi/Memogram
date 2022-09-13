package com.dapascript.memogram.data.source.local.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dapascript.memogram.data.source.local.model.FeedEntity

@Dao
interface FeedDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeed(feed: List<FeedEntity>)

    @Query("SELECT * FROM feed")
    fun getFeed(): PagingSource<Int, FeedEntity>

    @Query("DELETE FROM feed")
    suspend fun deleteFeed()
}