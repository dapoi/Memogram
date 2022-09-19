package com.dapascript.memogram.presentation.ui.story

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.dapascript.memogram.data.source.UserRepositoryImpl
import com.dapascript.memogram.utils.CoroutinesTestRule
import com.dapascript.memogram.utils.DataDummy
import com.dapascript.memogram.utils.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class UploadStoryViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutinesTestRule = CoroutinesTestRule()

    @Mock
    private lateinit var repositoryImpl: UserRepositoryImpl
    private lateinit var uploadStoryViewModel: UploadStoryViewModel

    private val uploadResponse = DataDummy.dummyPhoto()
    private val tokenDummy = "token"
    private val photoDummy = MultipartBody.Part.create("photo".toRequestBody())
    private val descDummy = "desc".toRequestBody()
    private val latDummy = "lat".toRequestBody()
    private val longDummy = "long".toRequestBody()

    @Before
    fun setUp() {
        uploadStoryViewModel = UploadStoryViewModel(repositoryImpl)
    }

    @Test
    fun `Upload Success`() = runTest {
        val result = flowOf(Resource.Success(uploadResponse))
        Mockito.`when`(
            repositoryImpl.postStory(
                tokenDummy,
                photoDummy,
                descDummy,
                latDummy,
                longDummy
            )
        ).thenReturn(result)

        uploadStoryViewModel.postStory(
            tokenDummy,
            photoDummy,
            descDummy,
            latDummy,
            longDummy
        ).observeForever {
            assert(it.data == uploadResponse)
        }

        Mockito.verify(repositoryImpl).postStory(
            tokenDummy,
            photoDummy,
            descDummy,
            latDummy,
            longDummy
        )
    }

    @Test
    fun `Upload Failed`() = runTest {
        val result = flowOf(Resource.Error("Error", null))
        Mockito.`when`(
            repositoryImpl.postStory(
                tokenDummy,
                photoDummy,
                descDummy,
                latDummy,
                longDummy
            )
        ).thenReturn(result)

        uploadStoryViewModel.postStory(
            tokenDummy,
            photoDummy,
            descDummy,
            latDummy,
            longDummy
        ).observeForever {
            assert(it.message == "Error")
        }

        Mockito.verify(repositoryImpl).postStory(
            tokenDummy,
            photoDummy,
            descDummy,
            latDummy,
            longDummy
        )
    }
}