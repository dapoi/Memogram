package com.dapascript.memogram.data.source

import androidx.paging.PagingData
import com.dapascript.memogram.data.source.local.model.FeedEntity
import com.dapascript.memogram.data.source.remote.model.FeedResponse
import com.dapascript.memogram.data.source.remote.model.UploadResponse
import com.dapascript.memogram.utils.Resource
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface StoryRepository {

    fun getFeed(
        token: String
    ): Flow<PagingData<FeedEntity>>

    fun getFeedLocation(token: String): Flow<Resource<FeedResponse>>

    fun postStory(
        token: String,
        photo: MultipartBody.Part,
        desc: RequestBody,
        lat: RequestBody?,
        lon: RequestBody?
    ): Flow<Resource<UploadResponse>>
}