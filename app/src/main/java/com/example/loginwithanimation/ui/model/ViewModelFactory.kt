package com.example.loginwithanimation.ui.model

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.loginwithanimation.data.retrofit.ApiConfig
import com.example.loginwithanimation.data.preferences.SessionPreferences
import com.example.loginwithanimation.repository.AuthRepository
import com.example.loginwithanimation.repository.StoryRepository

class ViewModelFactory(private val context: Context) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val pref = SessionPreferences(context)
        val apiService = ApiConfig.getApiService()
        val authRepository = AuthRepository(pref)
        val storyRepository = StoryRepository(apiService)

        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(authRepository) as T
            }
            modelClass.isAssignableFrom(StoryViewModel::class.java) -> {
                StoryViewModel(storyRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
