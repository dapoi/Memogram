package com.dapascript.memogram.data.source

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.ExperimentalPagingApi
import com.dapascript.memogram.presentation.ui.feed.PagedFeedSource
import com.dapascript.memogram.utils.CoroutinesTestRule
import com.dapascript.memogram.utils.DataDummy
import com.dapascript.memogram.utils.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@ExperimentalPagingApi
@RunWith(MockitoJUnitRunner::class)
class UserRepositoryImplTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutinesTestRule = CoroutinesTestRule()

    @Mock
    private lateinit var mockRepositoryImpl: UserRepositoryImpl

    private val uploadResponse = DataDummy.dummyPhoto()
    private val tokenDummy = "token"
    private val photoDummy = MultipartBody.Part.create("photo".toRequestBody())
    private val descDummy = "desc".toRequestBody()
    private val latDummy = "lat".toRequestBody()
    private val longDummy = "long".toRequestBody()

    @Test
    fun `Login User`() = runTest {
        val result = flowOf(Resource.Success(DataDummy.dummyLogin()))
        Mockito.`when`(mockRepositoryImpl.loginUser("test", "test")).thenReturn(result)

        mockRepositoryImpl.loginUser("test", "test").collect {
            Assert.assertEquals(it.data, DataDummy.dummyLogin())
        }
    }

    @Test
    fun `Register User`() = runTest {
        val result = flowOf(Resource.Success(DataDummy.dummyRegister()))
        Mockito.`when`(mockRepositoryImpl.registerUser("test", "test", "test")).thenReturn(result)

        mockRepositoryImpl.registerUser("test", "test", "test").collect {
            Assert.assertEquals(it.data, DataDummy.dummyRegister())
        }
    }

    @Test
    fun `Get Feed`() = runTest {
        val dataDummy = DataDummy.dummyFeed()
        val dataPaging = PagedFeedSource.snapshot(dataDummy)
        val result = flowOf(dataPaging)
        Mockito.`when`(mockRepositoryImpl.getFeed("token")).thenReturn(result)

        mockRepositoryImpl.getFeed("token").collect {
            Assert.assertEquals(it, dataPaging)
        }
    }

    @Test
    fun `Get Feed Location`() = runTest {
        val result = flowOf(Resource.Success(DataDummy.dummyLocation()))
        Mockito.`when`(mockRepositoryImpl.getFeedLocation("token")).thenReturn(result)

        mockRepositoryImpl.getFeedLocation("token").collect {
            Assert.assertEquals(it.data, DataDummy.dummyLocation())
        }
    }

    @Test
    fun `Upload Photo`() = runTest {
        val result = flowOf(Resource.Success(uploadResponse))
        Mockito.`when`(
            mockRepositoryImpl.postStory(
                tokenDummy,
                photoDummy,
                descDummy,
                latDummy,
                longDummy
            )
        ).thenReturn(result)

        mockRepositoryImpl.postStory(
            tokenDummy, photoDummy, descDummy, latDummy, longDummy
        ).collect {
            Assert.assertEquals(it.data, uploadResponse)
        }
    }
}