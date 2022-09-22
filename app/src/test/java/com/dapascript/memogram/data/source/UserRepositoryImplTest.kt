package com.dapascript.memogram.data.source

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.asLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.ExperimentalPagingApi
import com.dapascript.memogram.presentation.adapter.FeedAdapter
import com.dapascript.memogram.presentation.ui.feed.PagedFeedSource
import com.dapascript.memogram.presentation.ui.feed.noopListUpdateCallback
import com.dapascript.memogram.utils.CoroutinesTestRule
import com.dapascript.memogram.utils.DataDummy
import com.dapascript.memogram.utils.Resource
import com.dapascript.memogram.utils.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@ExperimentalPagingApi
@RunWith(MockitoJUnitRunner.Silent::class)
class UserRepositoryImplTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutinesTestRule = CoroutinesTestRule()

    private lateinit var repositoryImpl: UserRepositoryImpl

    private val uploadResponse = DataDummy.dummyPhoto()
    private val locationResponse = DataDummy.dummyLocation()
    private val tokenDummy = "authentication_token"
    private val photoDummy = MultipartBody.Part.create("text".toRequestBody())
    private val descDummy = "desc".toRequestBody()
    private val nameDummy = "dummyName"
    private val emailDummy = "mail@mail.com"
    private val passwordDummy = "password"

    @Before
    fun setUp() = runBlocking {
        repositoryImpl = mock(UserRepositoryImpl::class.java)
    }

    @Test
    fun `Login User`(): Unit = runTest {
        val dataDummy = DataDummy.dummyLogin()
        `when`(
            repositoryImpl.loginUser(
                emailDummy,
                passwordDummy
            )
        ).thenReturn(flowOf(Resource.Success(dataDummy)))

        repositoryImpl.loginUser(emailDummy, passwordDummy).asLiveData().observeForever {
            Assert.assertEquals(dataDummy, it.data)
        }
    }

    @Test
    fun `Register User`() = runTest {
        val dataDummy = DataDummy.dummyRegister()
        `when`(repositoryImpl.registerUser(nameDummy, emailDummy, passwordDummy)).thenReturn(
            flowOf(Resource.Success(dataDummy))
        )

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

        `when`(
            repositoryImpl.postStory(
                tokenDummy,
                photoDummy,
                descDummy,
                null,
                null
            )
        ).thenReturn(flowOf(Resource.Success(result)))

        repositoryImpl.postStory(tokenDummy, photoDummy, descDummy, null, null).asLiveData()
            .observeForever {
                Assert.assertNotNull(it)
                Assert.assertTrue(it is Resource.Success)
                Assert.assertEquals(Resource.Success(result), it)
            }
    }

    @Test
    fun `Get Feed`() = runTest {
        val dummy = DataDummy.dummyFeed()
        val dataPaging = PagedFeedSource.snapshot(dummy)
        val result = flowOf(dataPaging)

        `when`(repositoryImpl.getFeed(tokenDummy)).thenReturn(
            result
        )

        val actual = repositoryImpl.getFeed(tokenDummy).asLiveData().getOrAwaitValue()
        val dif = AsyncPagingDataDiffer(
            diffCallback = FeedAdapter.DIFF_UTIL,
            updateCallback = noopListUpdateCallback,
            mainDispatcher = coroutinesTestRule.testDispatcher,
            workerDispatcher = coroutinesTestRule.testDispatcher
        )
        dif.submitData(actual)
        Assert.assertEquals(dummy.size, dif.snapshot().size)
    }

    @Test
    fun `Get Feed Location`() = runTest {
        val result = locationResponse
        `when`(repositoryImpl.getFeedLocation(tokenDummy)).thenReturn(flowOf(Resource.Success(result)))

        repositoryImpl.getFeedLocation(tokenDummy).asLiveData().observeForever {
            Assert.assertNotNull(it)
            Assert.assertTrue(it is Resource.Success)
            Assert.assertEquals(Resource.Success(result), it)
        }
    }
}