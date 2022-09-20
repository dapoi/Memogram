package com.dapascript.memogram.data.source

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.asLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.ExperimentalPagingApi
import com.dapascript.memogram.data.source.local.db.FeedDatabase
import com.dapascript.memogram.data.source.remote.network.ApiService
import com.dapascript.memogram.presentation.adapter.FeedAdapter
import com.dapascript.memogram.presentation.ui.feed.PagedFeedSource
import com.dapascript.memogram.presentation.ui.feed.noopListUpdateCallback
import com.dapascript.memogram.utils.CoroutinesTestRule
import com.dapascript.memogram.utils.DataDummy
import com.dapascript.memogram.utils.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.Assert
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
class UserRepositoryImplTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutinesTestRule = CoroutinesTestRule()

    @Mock
    private lateinit var apiService: ApiService

    @Mock
    private lateinit var feedDB: FeedDatabase

    @Mock
    private lateinit var repositoryImplMock: UserRepositoryImpl

    private lateinit var repositoryImpl: UserRepositoryImpl

    private val uploadResponse = DataDummy.dummyPhoto()
    private val locationResponse = DataDummy.dummyLocation()
    private val tokenDummy = "authentication_token"
    private val photoDummy = MultipartBody.Part.create("text".toRequestBody())
    private val descDummy = "desc".toRequestBody()
    private val nameDummy = "dummyName"
    private val emailDummy = "mail@mail.com"
    private val passwordDummy = "password"
    private val dataUser = mapOf(
        "email" to emailDummy,
        "password" to passwordDummy
    )

    @Before
    fun setUp() {
        repositoryImpl = UserRepositoryImpl(apiService, feedDB)
    }

    @Test
    fun `Login User`(): Unit = runTest {
        val dataDummy = DataDummy.dummyLogin()
        Mockito.`when`(apiService.loginUser(dataUser)).thenReturn(dataDummy)

        repositoryImpl.loginUser(emailDummy, passwordDummy).asLiveData().observeForever {
            Assert.assertNotNull(it)
            Assert.assertTrue(it is Resource.Success)
            Assert.assertEquals(Resource.Success(dataDummy), it)
        }
    }

    @Test
    fun `Register User`() = runTest {
        val dataDummy = DataDummy.dummyRegister()
        Mockito.`when`(apiService.registerUser(dataUser)).thenReturn(dataDummy)

        repositoryImpl.registerUser(nameDummy, emailDummy, passwordDummy).asLiveData()
            .observeForever {
                Assert.assertNotNull(it)
                Assert.assertTrue(it is Resource.Success)
                Assert.assertEquals(Resource.Success(dataDummy), it)
            }
    }

    @Test
    fun `Upload Photo`() = runTest {
        val result = uploadResponse
        Mockito.`when`(
            apiService.postStory(
                tokenDummy,
                photoDummy,
                descDummy,
                null,
                null
            )
        ).thenReturn(result)

        repositoryImpl.postStory(
            tokenDummy,
            photoDummy,
            descDummy,
            null,
            null
        ).collect { response ->
            response.data?.let {
                Assert.assertEquals(uploadResponse, it)
            }
        }
    }

    @Test
    fun `Get Feed`() = runTest {
        val dataDummy = DataDummy.dummyFeed()
        val dataPaging = PagedFeedSource.snapshot(dataDummy)
        val result = flowOf(dataPaging)

        Mockito.`when`(repositoryImplMock.getFeed(tokenDummy)).thenReturn(result)

        repositoryImplMock.getFeed(tokenDummy).collect {
            val differ = AsyncPagingDataDiffer(
                diffCallback = FeedAdapter.DIFF_UTIL,
                updateCallback = noopListUpdateCallback,
                mainDispatcher = coroutinesTestRule.testDispatcher,
                workerDispatcher = coroutinesTestRule.testDispatcher
            )
            differ.submitData(it)

            Assert.assertNotNull(differ.snapshot())
            Assert.assertEquals(dataDummy.size, differ.snapshot().size)
        }
    }

    @Test
    fun `Get Feed Location`() = runTest {
        val result = locationResponse
        Mockito.`when`(apiService.getFeed(tokenDummy)).thenReturn(result)

        repositoryImpl.getFeedLocation(tokenDummy).collect { response ->
            response.data?.let {
                Assert.assertEquals(locationResponse, it)
            }
        }
    }
}