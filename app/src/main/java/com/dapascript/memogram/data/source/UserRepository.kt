package com.dapascript.memogram.data.source

import com.dapascript.memogram.data.source.remote.model.LoginResponse
import com.dapascript.memogram.data.source.remote.model.RegisterResponse
import com.dapascript.memogram.utils.Resource
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    fun loginUser(
        email: String,
        password: String
    ): Flow<Resource<LoginResponse>>

    fun registerUser(
        name: String,
        email: String,
        password: String,
    ): Flow<Resource<RegisterResponse>>
}