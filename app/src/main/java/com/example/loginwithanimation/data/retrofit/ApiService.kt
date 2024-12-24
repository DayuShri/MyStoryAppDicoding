package com.example.loginwithanimation.data.retrofit

import com.example.loginwithanimation.data.response.GenericResponse
import com.example.loginwithanimation.data.response.LoginResponse
import com.example.loginwithanimation.data.response.RegisterResponse
import com.example.loginwithanimation.data.response.StoriesResponse
import com.example.loginwithanimation.data.response.StoryDetailResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiService {
    @POST("register")
    suspend fun register(@Body body: Map<String, String>
    ): RegisterResponse

    @POST("login")
    suspend fun login(@Body body: Map<String, String>
    ): LoginResponse

    @GET("stories/{id}")
    suspend fun getStoryDetail(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): StoryDetailResponse

    @Multipart
    @POST("stories")
    suspend fun addStory(
        @Header("Authorization") token: String,
        @Part("description") description: RequestBody,
        @Part photo: MultipartBody.Part,
        @Part("lon") lon: RequestBody? = null,
        @Part("lat") lat: RequestBody? = null
    ): GenericResponse

    @GET("stories")
    suspend fun getStoriesWithLocation(
        @Header("Authorization") token: String,
        @Query("location") location: Int = 1
    ): StoriesResponse

    @GET("stories")
    suspend fun getAllStoriesWithPaging(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): StoriesResponse
}
