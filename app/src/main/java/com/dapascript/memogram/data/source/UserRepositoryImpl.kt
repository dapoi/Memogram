package com.dapascript.memogram.data.source

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dapascript.memogram.data.mediator.FeedMediator
import com.dapascript.memogram.data.source.local.db.FeedDatabase
import com.dapascript.memogram.data.source.local.model.FeedEntity
import com.dapascript.memogram.data.source.remote.model.LoginResponse
import com.dapascript.memogram.data.source.remote.model.RegisterResponse
import com.dapascript.memogram.data.source.remote.model.UploadResponse
import com.dapascript.memogram.data.source.remote.network.ApiService
import com.dapascript.memogram.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val feedDB: FeedDatabase
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

    override fun getFeed(token: String): Flow<PagingData<FeedEntity>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(pageSize = 10),
            remoteMediator = FeedMediator(
                apiService,
                feedDB,
                "Bearer $token"
            ),
            pagingSourceFactory = { feedDB.feedDao().getFeed() }
        ).flow
    }

    override fun postStory(
        token: String,
        photo: MultipartBody.Part,
        desc: RequestBody
    ): Flow<Resource<UploadResponse>> {
        return flow {
            emit(Resource.Loading())
            try {
                val response = apiService.postStory("Bearer $token", photo, desc)
                emit(Resource.Success(response))
            } catch (e: Exception) {
                emit(Resource.Error(e.message.toString()))
            }
        }
    }
}