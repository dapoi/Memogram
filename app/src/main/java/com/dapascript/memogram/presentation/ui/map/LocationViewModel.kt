package com.dapascript.memogram.presentation.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.dapascript.memogram.data.source.UserRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val repositoryImpl: UserRepositoryImpl
) : ViewModel() {
    fun getLocation(token: String) = repositoryImpl.getFeedLocation(token).asLiveData()
}