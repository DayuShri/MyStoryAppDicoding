package com.example.loginwithanimation.ui.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.loginwithanimation.data.response.GenericResponse
import com.example.loginwithanimation.data.response.Story
import com.example.loginwithanimation.data.response.StoryDetailResponse
import com.example.loginwithanimation.data.response.StoryItem
import com.example.loginwithanimation.repository.StoryRepository
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody

class StoryViewModel(private val repository: StoryRepository) : ViewModel() {

    private val _pagingState = MutableLiveData(false)

    val pagingState: LiveData<Boolean> get() = _pagingState

    fun resetPagingState() {
        _pagingState.value = false
    }

    fun fetchStoriesWithLocation(token: String): LiveData<List<StoryItem>> = liveData {
        try {
            val storiesWithLocation = repository.getStoriesWithLocation(token)
            emit(storiesWithLocation)
        } catch (e: Exception) {
            Log.e("StoryViewModel", "Error fetching stories: ${e.message}")
            emit(emptyList())
        }
    }

    fun getStoryDetail(token: String, id: String): LiveData<StoryDetailResponse> = liveData {
        try {
            val response = repository.getStoryDetail(token, id)
            emit(response)
        } catch (e: Exception) {
            emit(
                StoryDetailResponse(
                    error = true,
                    message = e.message ?: "An error occurred",
                    story = Story(
                        id = "",
                        name = "",
                        description = "",
                        photoUrl = "",
                        createdAt = "",
                        lat = null,
                        lon = null
                    )
                )
            )
        }
    }

    fun addStory(
        token: String,
        description: RequestBody,
        photo: MultipartBody.Part,
        lat: RequestBody? = null,
        lon: RequestBody? = null
    ): LiveData<GenericResponse> = liveData {
        emit(GenericResponse(error = false, message = "Uploading..."))
        try {
            emit(repository.addStory(token, description, photo, lat, lon))
            _pagingState.postValue(true)
        } catch (e: Exception) {
            emit(GenericResponse(error = true, message = e.message ?: "Upload failed"))
            _pagingState.postValue(false)
        }
    }

    fun getStoriesPaging(token: String): Flow<PagingData<StoryItem>> {
        return repository.getStoriesPaging(token).cachedIn(viewModelScope)
    }
}
