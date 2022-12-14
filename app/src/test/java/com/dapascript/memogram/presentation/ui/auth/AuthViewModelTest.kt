package com.dapascript.memogram.presentation.ui.auth

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.dapascript.memogram.data.source.UserRepositoryImpl
import com.dapascript.memogram.utils.CoroutinesTestRule
import com.dapascript.memogram.utils.DataDummy
import com.dapascript.memogram.utils.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class AuthViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutinesTestRule = CoroutinesTestRule()

    @Mock
    private lateinit var userRepository: UserRepositoryImpl
    private lateinit var authViewModel: AuthViewModel

    private val dummyRegister = DataDummy.dummyRegister()
    private val dummyLogin = DataDummy.dummyLogin()

    @Before
    fun setUp() {
        authViewModel = AuthViewModel(userRepository)
    }

    @Test
    fun `Register Success`() = runTest {
        val result = flowOf(Resource.Success(dummyRegister))
        Mockito.`when`(
            userRepository.registerUser(
                "test", "email", "password"
            )
        ).thenReturn(result)

        authViewModel.registerUser("test", "email", "password").observeForever {
            Assert.assertNotNull(it)
            Assert.assertEquals(Resource.Success(dummyRegister), it)
            Assert.assertEquals(dummyRegister, it.data)
        }

        Mockito.verify(userRepository).registerUser("test", "email", "password")
    }

    @Test
    fun `Register Failed`() = runTest {
        val result = flowOf(Resource.Error("Error", null))
        Mockito.`when`(
            userRepository.registerUser(
                "test", "email", "password"
            )
        ).thenReturn(result)

        authViewModel.registerUser("test", "email", "password").observeForever {
            Assert.assertEquals(Resource.Error("Error", null), it)
        }

        Mockito.verify(userRepository).registerUser("test", "email", "password")
    }

    @Test
    fun `Login Success`() = runTest {
        val result = flowOf(Resource.Success(dummyLogin))
        Mockito.`when`(
            userRepository.loginUser(
                "email", "password"
            )
        ).thenReturn(result)

        authViewModel.loginUser("email", "password").observeForever {
            Assert.assertNotNull(it)
            Assert.assertEquals(Resource.Success(dummyLogin), it)
            Assert.assertEquals(dummyLogin, it.data)
        }

        Mockito.verify(userRepository).loginUser("email", "password")
    }

    @Test
    fun `Login Failed`() = runTest {
        val result = flowOf(Resource.Error("Error", null))
        Mockito.`when`(
            userRepository.loginUser(
                "email", "password"
            )
        ).thenReturn(result)

        authViewModel.loginUser("email", "password").observeForever {
            Assert.assertEquals(Resource.Error("Error", null), it)
        }

        Mockito.verify(userRepository).loginUser("email", "password")
    }
}