package com.dapascript.memogram.presentation.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.dapascript.memogram.data.source.StoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val storyRepository: StoryRepository
) : ViewModel() {
    fun getLocation(token: String) = storyRepository.getFeedLocation(token).asLiveData()
}