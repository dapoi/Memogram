package com.dapascript.memogram.presentation.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.dapascript.memogram.data.source.StoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val storyRepository: StoryRepository
) : ViewModel() {

    fun getFeed(token: String) = storyRepository.getFeed(token).cachedIn(viewModelScope)
}