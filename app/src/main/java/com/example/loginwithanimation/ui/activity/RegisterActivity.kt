package com.example.loginwithanimation.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.loginwithanimation.databinding.ActivityRegisterBinding
import com.example.loginwithanimation.ui.model.AuthState
import com.example.loginwithanimation.ui.model.AuthViewModel
import com.example.loginwithanimation.ui.model.ViewModelFactory
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels { ViewModelFactory(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signupButton.setOnClickListener {
            val name = binding.nameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (name.isEmpty()) {
                binding.nameEditTextLayout.error = "Nama tidak boleh kosong"
                return@setOnClickListener
            } else {
                binding.nameEditTextLayout.error = null
            }
            if (binding.emailEditText.error != null || binding.passwordEditText.error != null) {
                Toast.makeText(this, "Perbaiki input sebelum mendaftar!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.register(name, email, password)
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authState.collect { state ->
                    when (state) {
                        is AuthState.Loading -> {
                            Toast.makeText(this@RegisterActivity, "Loading...", Toast.LENGTH_SHORT).show()
                        }
                        is AuthState.Success -> {
                            Toast.makeText(this@RegisterActivity, state.message, Toast.LENGTH_LONG).show()
                            finish()
                        }
                        is AuthState.Error -> {
                            Toast.makeText(this@RegisterActivity, state.message, Toast.LENGTH_LONG).show()
                        }
                        else -> Unit
                    }
                }
            }
        }
    }
}
