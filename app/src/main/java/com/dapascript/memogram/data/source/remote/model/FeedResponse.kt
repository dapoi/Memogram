package com.dapascript.memogram.data.source.remote.model

import com.squareup.moshi.Json

data class FeedResponse(

    @Json(name = "listStory")
    val listStory: List<ListStoryItem>,

    @Json(name = "error")
    val error: Boolean,

    @Json(name = "message")
    val message: String
)

data class ListStoryItem(

    @Json(name = "photoUrl")
    val photoUrl: String,

    @Json(name = "createdAt")
    val createdAt: String,

    @Json(name = "name")
    val name: String,

    @Json(name = "description")
    val description: String,

    @Json(name = "id")
    val id: String,

    @Json(name = "lat")
    val lat: Double? = null,

    @Json(name = "lon")
    val lon: Double? = null,
)
