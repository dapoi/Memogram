package com.dapascript.memogram.data.source.remote.model

import com.squareup.moshi.Json

data class RegisterResponse(

	@Json(name="error")
	val error: Boolean,

	@Json(name="message")
	val message: String
)
