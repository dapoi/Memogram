package com.dapascript.memogram.data.source.remote.network

import com.dapascript.memogram.data.source.remote.model.FeedResponse
import com.dapascript.memogram.data.source.remote.model.LoginResponse
import com.dapascript.memogram.data.source.remote.model.RegisterResponse
import com.dapascript.memogram.data.source.remote.model.UploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiService {

    /**
     * Register user
     */
    @POST("register")
    suspend fun registerUser(
        @Body user: Map<String, String>
    ): RegisterResponse

    /**
     * Login user
     */
    @POST("login")
    suspend fun loginUser(
        @Body user: Map<String, String>
    ): LoginResponse

    /**
     * Get all stories
     */
    @GET("stories")
    suspend fun getFeed(
        @Header("Authorization") token: String,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
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
    ): UploadResponse
}