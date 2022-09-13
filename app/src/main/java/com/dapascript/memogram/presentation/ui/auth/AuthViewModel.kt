package com.dapascript.memogram.presentation.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.dapascript.memogram.data.source.UserRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repositoryImpl: UserRepositoryImpl,
) : ViewModel() {

    fun loginUser(
        email: String, password: String
    ) = repositoryImpl.loginUser(email, password).asLiveData()

    fun registerUser(
        name: String,
        email: String,
        password: String
    ) = repositoryImpl.registerUser(name, email, password).asLiveData()
}