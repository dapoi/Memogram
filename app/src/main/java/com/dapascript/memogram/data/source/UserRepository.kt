package com.dapascript.memogram.data.source

import androidx.paging.PagingData
import com.dapascript.memogram.data.source.local.model.FeedEntity
import com.dapascript.memogram.data.source.remote.model.LoginResponse
import com.dapascript.memogram.data.source.remote.model.RegisterResponse
import com.dapascript.memogram.data.source.remote.model.UploadResponse
import com.dapascript.memogram.utils.Resource
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody

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

    fun getFeed(
        token: String
    ): Flow<PagingData<FeedEntity>>

    fun postStory(
        token: String,
        photo: MultipartBody.Part,
        desc: RequestBody
    ): Flow<Resource<UploadResponse>>
}