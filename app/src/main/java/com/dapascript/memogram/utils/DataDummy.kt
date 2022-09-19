package com.dapascript.memogram.utils

import com.dapascript.memogram.data.source.local.model.FeedEntity
import com.dapascript.memogram.data.source.remote.model.*

object DataDummy {

    fun dummyRegister(): RegisterResponse {
        return RegisterResponse(
            error = false,
            message = "Success",
        )
    }

    fun dummyLogin(): LoginResponse {
        val loginResult = LoginResult(
            name = "Daffa",
            userId = "1",
            token = "token"
        )

        return LoginResponse(
            error = false,
            message = "Success",
            loginResult = loginResult
        )
    }

    fun dummyPhoto(): UploadResponse {
        return UploadResponse(
            error = false,
            message = "Success",
        )
    }

    fun dummyFeed(): List<FeedEntity> {
        val listStoryItem = ArrayList<FeedEntity>()
        for (i in 1..10) {
            val feed = FeedEntity(
                id = "id$i",
                name = "name$i",
                image = "photoUrl$i",
                date = "createdAt$i",
                description = "description$i",
                lat = 0.0,
                lon = 0.0,
            )
            listStoryItem.add(feed)
        }
        return listStoryItem
    }

    fun dummyLocation(): FeedResponse {
        val listStoryItem = ArrayList<ListStoryItem>()
        for (i in 1..5) {
            val feed = ListStoryItem(
                id = "id$i",
                name = "name$i",
                photoUrl = "photoUrl$i",
                createdAt = "createdAt$i",
                description = "description$i",
                lat = 0.0,
                lon = 0.0,
            )
            listStoryItem.add(feed)
        }

        return FeedResponse(
            listStory = listStoryItem,
            error = false,
            message = "Success",
        )
    }
}