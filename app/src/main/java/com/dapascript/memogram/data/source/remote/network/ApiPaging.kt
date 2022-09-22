package com.dapascript.memogram.data.source.remote.network

import com.dapascript.memogram.data.source.remote.model.FeedResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ApiPaging {
    @GET("stories")
    suspend fun getFeed(
        @Header("Authorization") token: String,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("location") location: Int? = null,
    ): FeedResponse
}