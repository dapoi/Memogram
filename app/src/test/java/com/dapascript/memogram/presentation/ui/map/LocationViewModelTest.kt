package com.dapascript.memogram.presentation.ui.map

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.dapascript.memogram.data.source.UserRepositoryImpl
import com.dapascript.memogram.utils.CoroutinesTestRule
import com.dapascript.memogram.utils.DataDummy
import com.dapascript.memogram.utils.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class LocationViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutinesTestRule = CoroutinesTestRule()

    @Mock
    private lateinit var repositoryImpl: UserRepositoryImpl
    private lateinit var locationViewModel: LocationViewModel

    private var tokenDummy = "tokenDummy"

    @Before
    fun setUp() {
        locationViewModel = LocationViewModel(repositoryImpl)
    }

    @Test
    fun `Get User Location`() {
        val result = flowOf(Resource.Success(DataDummy.dummyLocation()))
        Mockito.`when`(repositoryImpl.getFeedLocation(tokenDummy)).thenReturn(result)

        locationViewModel.getLocation(tokenDummy).observeForever {
            assert(it.data == DataDummy.dummyLocation())
        }

        Mockito.verify(repositoryImpl).getFeedLocation(tokenDummy)
    }

    @Test
    fun `Get User Location Error`() {
        val result = flowOf(Resource.Error("Error", null))
        Mockito.`when`(repositoryImpl.getFeedLocation(tokenDummy)).thenReturn(result)

        locationViewModel.getLocation(tokenDummy).observeForever {
            assert(it.message == "Error")
        }

        Mockito.verify(repositoryImpl).getFeedLocation(tokenDummy)
    }
}