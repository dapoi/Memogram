package com.dapascript.memogram.data.source

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dapascript.memogram.data.mediator.FeedMediator
import com.dapascript.memogram.data.source.local.db.FeedDatabase
import com.dapascript.memogram.data.source.local.model.FeedEntity
import com.dapascript.memogram.data.source.remote.model.FeedResponse
import com.dapascript.memogram.data.source.remote.model.UploadResponse
import com.dapascript.memogram.data.source.remote.network.ApiPaging
import com.dapascript.memogram.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class StoryRepositoryImpl @Inject constructor(
    private val apiPaging: ApiPaging,
    private val feedDB: FeedDatabase
) : StoryRepository {
    override fun getFeed(token: String): Flow<PagingData<FeedEntity>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(pageSize = 10),
            remoteMediator = FeedMediator(
                apiPaging,
                feedDB,
                "Bearer $token"
            ),
            pagingSourceFactory = { feedDB.feedDao().getFeed() }
        ).flow
    }

    override fun getFeedLocation(token: String): Flow<Resource<FeedResponse>> {
        return channelFlow {
            send(Resource.Loading())
            try {
                val response = apiPaging.getFeed(
                    token = "Bearer $token",
                    size = 30,
                    location = 1
                )
                send(Resource.Success(response))
            }catch (e: Exception){
                send(Resource.Error(e.message.toString()))
            }
        }
    }

    override fun postStory(
        token: String,
        photo: MultipartBody.Part,
        desc: RequestBody,
        lat: RequestBody?,
        lon: RequestBody?
    ): Flow<Resource<UploadResponse>> {
        return flow {
            emit(Resource.Loading())
            try {
                val response = apiPaging.postStory("Bearer $token", photo, desc, lat, lon)
                emit(Resource.Success(response))
            } catch (e: Exception) {
                emit(Resource.Error(e.message.toString()))
            }
        }
    }
}