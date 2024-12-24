package com.example.loginwithanimation.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.loginwithanimation.data.preferences.SessionPreferences
import com.example.loginwithanimation.databinding.ActivityLoginBinding
import com.example.loginwithanimation.ui.model.AuthState
import com.example.loginwithanimation.ui.model.AuthViewModel
import com.example.loginwithanimation.ui.model.ViewModelFactory
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels { ViewModelFactory(this) }
    private lateinit var sessionPreferences: SessionPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionPreferences = SessionPreferences(this)

        binding.btnLogin.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (binding.emailEditText.error != null || binding.passwordEditText.error != null) {
                Toast.makeText(this, "Perbaiki input sebelum login!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan password tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.login(email, password)
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authState.collect { state ->
                    when (state) {
                        is AuthState.Loading -> {
                            Toast.makeText(this@LoginActivity, "Loading...", Toast.LENGTH_SHORT).show()
                        }
                        is AuthState.Success -> {
                            state.loginResult?.token?.let { token ->
                                lifecycleScope.launch {
                                    sessionPreferences.saveToken(token)
                                }
                            }
                            Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_LONG).show()

                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        is AuthState.Error -> {
                            Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_LONG).show()
                        }
                        else -> Unit
                    }
                }
            }
        }
    }
}
