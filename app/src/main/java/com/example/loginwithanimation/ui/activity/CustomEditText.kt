package com.example.loginwithanimation.ui.activity

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputEditText
import com.example.loginwithanimation.R

class CustomEditText(context: Context, attrs: AttributeSet?) : TextInputEditText(context, attrs) {

    init {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s.toString()
                when (id) {
                    R.id.emailEditText -> {
                        error = if (!input.contains("@")) {
                            "Email harus sesuai dengan format yang benar"
                        } else {
                            null
                        }
                    }
                    R.id.passwordEditText -> {
                        error = if (input.length < 8) {
                            "Password harus minimal 8 karakter"
                        } else {
                            null
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }
}