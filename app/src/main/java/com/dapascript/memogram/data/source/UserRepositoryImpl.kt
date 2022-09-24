package com.dapascript.memogram.data.source

import com.dapascript.memogram.data.source.remote.model.LoginResponse
import com.dapascript.memogram.data.source.remote.model.RegisterResponse
import com.dapascript.memogram.data.source.remote.network.ApiService
import com.dapascript.memogram.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
) : UserRepository {

    override fun loginUser(email: String, password: String): Flow<Resource<LoginResponse>> {
        return flow {
            emit(Resource.Loading())
            try {
                val user = mapOf(
                    "email" to email,
                    "password" to password
                )
                val response = apiService.loginUser(user)
                emit(Resource.Success(response))
            } catch (e: Exception) {
                emit(Resource.Error(e.message.toString()))
            }
        }
    }

    override fun registerUser(
        name: String,
        email: String,
        password: String
    ): Flow<Resource<RegisterResponse>> = flow {
        emit(Resource.Loading())
        try {
            val user = mapOf(
                "name" to name,
                "email" to email,
                "password" to password
            )
            val response = apiService.registerUser(user)
            emit(Resource.Success(response))
        } catch (e: Exception) {
            emit(Resource.Error(e.message.toString()))
        }
    }
}