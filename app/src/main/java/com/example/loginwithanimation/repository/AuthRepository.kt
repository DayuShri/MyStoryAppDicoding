package com.example.loginwithanimation.repository

import com.example.loginwithanimation.data.response.LoginResponse
import com.example.loginwithanimation.data.response.RegisterResponse
import com.example.loginwithanimation.data.retrofit.ApiConfig
import com.example.loginwithanimation.data.preferences.SessionPreferences
import com.google.gson.Gson
import retrofit2.HttpException

class AuthRepository(private val sessionPreferences: SessionPreferences) {

    suspend fun registerUser(name: String, email: String, password: String): RegisterResponse {
        return try {
            val requestBody = mapOf("name" to name, "email" to email, "password" to password)
            val response = ApiConfig.getApiService().register(requestBody)
            response
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorMessage = extractErrorMessage(errorBody)
            RegisterResponse(error = true, message = errorMessage)
        } catch (e: Exception) {
            RegisterResponse(error = true, message = "Terjadi kesalahan: ${e.message}")
        }
    }

    suspend fun loginUser(email: String, password: String): LoginResponse {
        return try {
            val requestBody = mapOf("email" to email, "password" to password)
            val response = ApiConfig.getApiService().login(requestBody)

            response.loginResult?.let {
                sessionPreferences.saveToken(it.token)
            }
            response
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorMessage = extractErrorMessage(errorBody)
            LoginResponse(error = true, message = errorMessage, loginResult = null)
        } catch (e: Exception) {
            LoginResponse(error = true, message = "Terjadi kesalahan: ${e.message}", loginResult = null)
        }
    }

    private fun extractErrorMessage(errorBody: String?): String {
        return try {
            val errorResponse = Gson().fromJson(errorBody, LoginResponse::class.java)
            errorResponse.message
        } catch (e: Exception) {
            "Terjadi Kesalahan yang tidak diketahui"
        }
    }
}