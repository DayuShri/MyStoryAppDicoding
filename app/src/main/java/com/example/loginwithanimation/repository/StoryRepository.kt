package com.example.loginwithanimation.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.loginwithanimation.data.paging.StoryPagingSource
import com.example.loginwithanimation.data.response.GenericResponse
import com.example.loginwithanimation.data.response.StoryDetailResponse
import com.example.loginwithanimation.data.response.StoryItem
import com.example.loginwithanimation.data.retrofit.ApiService
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody

class StoryRepository(private val apiService: ApiService) {

    suspend fun getStoryDetail(token: String, id: String): StoryDetailResponse {
        return apiService.getStoryDetail("Bearer $token", id)
    }

    suspend fun getStoriesWithLocation(token: String, location: Int = 1): List<StoryItem> {
        return try {
            val response = apiService.getStoriesWithLocation("Bearer $token", location)
            response.listStory
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun addStory(
        token: String,
        description: RequestBody,
        photo: MultipartBody.Part,
        lat: RequestBody? = null,
        lon: RequestBody? = null
    ): GenericResponse {
        return try {
            apiService.addStory("Bearer $token", description, photo, lat, lon)
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorMessage = extractErrorMessage(errorBody)
            GenericResponse(error = true, message = errorMessage)
        } catch (e: Exception) {
            GenericResponse(error = true, message = e.message ?: "Unexpected error occurred")
        }
    }

    fun getStoriesPaging(token: String): Flow<PagingData<StoryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { StoryPagingSource(apiService, token) }
        ).flow
    }

    private fun extractErrorMessage(errorBody: String?): String {
        return try {
            val errorResponse = Gson().fromJson(errorBody, GenericResponse::class.java)
            errorResponse.message
        } catch (e: Exception) {
            "Unknown error occurred"
        }
    }

    companion object {
        @Volatile
        private var instance: StoryRepository? = null

        fun getInstance(apiService: ApiService): StoryRepository {
            return instance ?: synchronized(this) {
                val newInstance = StoryRepository(apiService)
                instance = newInstance
                newInstance
            }
        }
    }
}
