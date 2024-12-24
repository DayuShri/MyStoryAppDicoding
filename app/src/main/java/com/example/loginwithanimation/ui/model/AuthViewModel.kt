package com.example.loginwithanimation.ui.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loginwithanimation.data.response.LoginResult
import com.example.loginwithanimation.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String, val loginResult: LoginResult? = null) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = authRepository.registerUser(name, email, password)
                _authState.value = if (response.error) {
                    AuthState.Error(response.message)
                } else {
                    AuthState.Success("Register Berhasil")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Register Gagal: ${e.message}")
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = authRepository.loginUser(email, password)
                _authState.value = if (response.error) {
                    AuthState.Error(response.message)
                } else {
                    AuthState.Success(
                        message = "Login Berhasil",
                        loginResult = response.loginResult
                    )
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Login Gagal: ${e.message}")
            }
        }
    }
}