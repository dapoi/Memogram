package com.dapascript.memogram.presentation.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.dapascript.memogram.data.source.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    fun loginUser(
        email: String, password: String
    ) = userRepository.loginUser(email, password).asLiveData()

    fun registerUser(
        name: String,
        email: String,
        password: String
    ) = userRepository.registerUser(name, email, password).asLiveData()
}