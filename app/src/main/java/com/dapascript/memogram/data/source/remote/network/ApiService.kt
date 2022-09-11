package com.dapascript.memogram.data.source.remote.network

import com.dapascript.memogram.data.source.remote.model.LoginResponse
import com.dapascript.memogram.data.source.remote.model.RegisterResponse
import retrofit2.http.Body
import retrofit2.http.POST

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
}