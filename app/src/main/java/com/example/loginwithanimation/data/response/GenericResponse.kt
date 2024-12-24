package com.example.loginwithanimation.data.response

import com.google.gson.annotations.SerializedName

data class GenericResponse(

	@field:SerializedName("error")
	val error: Boolean,

	@field:SerializedName("message")
	val message: String
)
