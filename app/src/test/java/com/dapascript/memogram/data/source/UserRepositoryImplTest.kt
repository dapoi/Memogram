package com.dapascript.memogram.data.source

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.asLiveData
import androidx.paging.ExperimentalPagingApi
import com.dapascript.memogram.data.source.remote.network.ApiService
import com.dapascript.memogram.utils.CoroutinesTestRule
import com.dapascript.memogram.utils.DataDummy
import com.dapascript.memogram.utils.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@ExperimentalPagingApi
@RunWith(MockitoJUnitRunner.Silent::class)
class UserRepositoryTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutinesTestRule = CoroutinesTestRule()

    @Mock
    private lateinit var apiService: ApiService

    private lateinit var userRepository: UserRepositoryImpl

    private val nameDummy = "dummyName"
    private val emailDummy = "mail@mail.com"
    private val passwordDummy = "password"

    @Before
    fun setUp() = runTest {
        userRepository = UserRepositoryImpl(apiService)
    }

    @Test
    fun `Login User`(): Unit = runTest {
        val dataDummy = DataDummy.dummyLogin()
        val dataUser = mapOf(
            "email" to emailDummy,
            "password" to passwordDummy
        )
        `when`(apiService.loginUser(dataUser)).thenReturn(dataDummy)

        userRepository.loginUser(emailDummy, passwordDummy).asLiveData().observeForever {
            Assert.assertNotNull(it)
            Assert.assertTrue(it is Resource.Success)
            Assert.assertEquals(Resource.Success(dataDummy), it.data)
        }
    }

    @Test
    fun `Register User`() = runTest {
        val dataDummy = DataDummy.dummyRegister()
        val dataUser = mapOf(
            "name" to nameDummy,
            "email" to emailDummy,
            "password" to passwordDummy
        )
        `when`(apiService.registerUser(dataUser)).thenReturn(dataDummy)

        userRepository.registerUser(nameDummy, emailDummy, passwordDummy).asLiveData()
            .observeForever {
                Assert.assertNotNull(it)
                Assert.assertTrue(it is Resource.Success)
                Assert.assertEquals(Resource.Success(dataDummy), it)
            }
    }
}