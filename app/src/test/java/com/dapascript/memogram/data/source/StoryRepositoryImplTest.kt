package com.dapascript.memogram.data.source

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.asLiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.room.Room
import com.dapascript.memogram.data.source.local.db.FeedDatabase
import com.dapascript.memogram.data.source.remote.network.ApiPaging
import com.dapascript.memogram.utils.CoroutinesTestRule
import com.dapascript.memogram.utils.DataDummy
import com.dapascript.memogram.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@ExperimentalPagingApi
@RunWith(MockitoJUnitRunner.Silent::class)
class StoryRepositoryImplTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutinesTestRule = CoroutinesTestRule()

    @Mock
    private lateinit var apiPaging: ApiPaging

    @Mock
    private lateinit var context: Context

    private lateinit var feedDB: FeedDatabase

    private lateinit var storyRepository: StoryRepositoryImpl

    private val uploadResponse = DataDummy.dummyPhoto()
    private val feedLocResponse = DataDummy.dummyLocationAndFeed()
    private val tokenDummy = "authentication_token"
    private val photoDummy = MultipartBody.Part.create("text".toRequestBody())
    private val descDummy = "desc".toRequestBody()

    @Before
    fun setUp() {
        feedDB = Room.inMemoryDatabaseBuilder(
            context,
            FeedDatabase::class.java
        ).allowMainThreadQueries().build()

        storyRepository = StoryRepositoryImpl(apiPaging, feedDB)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        feedDB.close()
    }

    @Test
    fun `Upload Photo`() = runTest {
        val result = uploadResponse

        Mockito.`when`(
            apiPaging.postStory(
                tokenDummy,
                photoDummy,
                descDummy,
                null,
                null
            )
        ).thenReturn(result)

        storyRepository.postStory(tokenDummy, photoDummy, descDummy, null, null).asLiveData()
            .observeForever {
                assertNotNull(it)
                assertTrue(it is Resource.Success)
                assertEquals(Resource.Success(result), it)
            }
    }

    @Test
    fun `Get Feed`() = runTest {
        val dummy = DataDummy.dummyFeed()
        val result = PagingData.from(dummy)

        storyRepository.getFeed(tokenDummy).asLiveData().observeForever {
            assertNotNull(it)
            assertEquals(Resource.Success(result), it)
        }
    }

    @Test
    fun `Get Feed Location`() = runTest {
        val result = feedLocResponse
        Mockito.`when`(apiPaging.getFeed(tokenDummy)).thenReturn(result)

        storyRepository.getFeedLocation(tokenDummy).asLiveData().observeForever {
            assertNotNull(it)
            assertTrue(it is Resource.Success)
            assertEquals(Resource.Success(result), it)
        }
    }
}