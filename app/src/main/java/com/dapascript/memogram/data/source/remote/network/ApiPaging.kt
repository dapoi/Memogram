package com.dapascript.memogram.data.source.remote.network

import com.dapascript.memogram.data.source.remote.model.FeedResponse
import com.dapascript.memogram.data.source.remote.model.UploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiPaging {

    /**
     * Get feed
     */
    @GET("stories")
    suspend fun getFeed(
        @Header("Authorization") token: String,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("location") location: Int? = null,
    ): FeedResponse

    /**
     * Post story
     */
    @Multipart
    @POST("stories")
    suspend fun postStory(
        @Header("Authorization") token: String,
        @Part photo: MultipartBody.Part,
        @Part("description") description: RequestBody,
        @Part("lat") lat: RequestBody?,
        @Part("lon") lon: RequestBody?,
    ): UploadResponse
}