package com.dapascript.memogram.data.mediator

import androidx.paging.*
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dapascript.memogram.data.source.local.db.FeedDatabase
import com.dapascript.memogram.data.source.local.model.FeedEntity
import com.dapascript.memogram.data.source.remote.model.FeedResponse
import com.dapascript.memogram.data.source.remote.model.ListStoryItem
import com.dapascript.memogram.data.source.remote.model.UploadResponse
import com.dapascript.memogram.data.source.remote.network.ApiPaging
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.junit.After
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExperimentalPagingApi
@RunWith(AndroidJUnit4::class)
class FeedMediatorTest {

    private var mockApi = FakeAPI()

    private var mockDB: FeedDatabase = Room.inMemoryDatabaseBuilder(
        getApplicationContext(),
        FeedDatabase::class.java,
    ).allowMainThreadQueries().build()

    @Test
    fun refreshLoadReturnsSuccessResultWhenMoreDataIsPresent() = runTest {
        val remoteMediator = FeedMediator(
            mockApi, mockDB, "test"
        )
        val pagingState = PagingState<Int, FeedEntity>(
            listOf(),
            null,
            PagingConfig(10),
            10
        )
        val result = remoteMediator.load(LoadType.REFRESH, pagingState)
        Assert.assertTrue(result is RemoteMediator.MediatorResult.Success)
        Assert.assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    @After
    fun tearDown() {
        mockDB.clearAllTables()
    }
}

class FakeAPI : ApiPaging {
    override suspend fun getFeed(
        token: String,
        page: Int?,
        size: Int?,
        location: Int?
    ): FeedResponse {
        val items: MutableList<ListStoryItem> = arrayListOf()
        var data: ListStoryItem
        for (i in 0..100) {
            data = ListStoryItem(
                id = i.toString(),
                name = "title",
                description = "description",
                photoUrl = "image",
                createdAt = "date",
                lat = 0.0,
                lon = 0.0,
            )
            items.add(data)
        }

        val subList = items.subList((page!! - 1) * size!!, (page - 1) * size + size)
        return FeedResponse(
            listStory = subList,
            error = false,
            message = "test"
        )
    }

    override suspend fun postStory(
        token: String,
        photo: MultipartBody.Part,
        description: RequestBody,
        lat: RequestBody?,
        lon: RequestBody?
    ): UploadResponse {
        return UploadResponse(
            error = false,
            message = "test"
        )
    }
}