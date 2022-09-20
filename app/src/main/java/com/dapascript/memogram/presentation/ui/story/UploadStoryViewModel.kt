package com.dapascript.memogram.presentation.ui.story

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.dapascript.memogram.data.source.UserRepository
import com.dapascript.memogram.data.source.remote.model.UploadResponse
import com.dapascript.memogram.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class UploadStoryViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    fun postStory(
        token: String,
        photo: MultipartBody.Part,
        description: RequestBody,
        lat: RequestBody,
        lon: RequestBody
    ): LiveData<Resource<UploadResponse>> {
        return userRepository.postStory(token, photo, description, lat, lon).asLiveData()
    }
}