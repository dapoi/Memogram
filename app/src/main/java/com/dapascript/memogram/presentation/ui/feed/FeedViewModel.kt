package com.dapascript.memogram.presentation.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.dapascript.memogram.data.source.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    fun getFeed(token: String) = userRepository.getFeed(token).cachedIn(viewModelScope)
}