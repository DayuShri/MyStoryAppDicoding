package com.example.loginwithanimation.ui.activity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.loginwithanimation.R
import com.example.loginwithanimation.data.preferences.SessionPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WelcomeActivity : AppCompatActivity() {
    private lateinit var sessionPreferences: SessionPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionPreferences = SessionPreferences(this)

        lifecycleScope.launch {
            val token = sessionPreferences.token.first()
            if (!token.isNullOrEmpty()) {
                startActivity(Intent(this@WelcomeActivity, MainActivity::class.java))
                finish()
            } else {
                setContentView(R.layout.activity_welcome)
                setupUI()
            }
        }
    }

    private fun setupUI() {
        val buttonRegister = findViewById<Button>(R.id.button_register)
        val buttonLogin = findViewById<Button>(R.id.button_login)

        buttonRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        buttonLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        playAnimation()
    }

    private fun playAnimation() {
        val bannerAnim = ObjectAnimator.ofFloat(findViewById<ImageView>
            (R.id.img_banner), View.ALPHA, 1f).apply {
            duration = 1000
        }
        val welcomeAnim = ObjectAnimator.ofFloat(findViewById<TextView>
            (R.id.tv_welcome), View.ALPHA, 1f).apply {
            duration = 1000
        }
        val descriptionAnim = ObjectAnimator.ofFloat(findViewById<TextView>
            (R.id.tv_description), View.ALPHA, 1f).apply {
            duration = 1000
        }
        val registerAnim = ObjectAnimator.ofFloat(findViewById<Button>
            (R.id.button_register), View.ALPHA, 1f).apply {
            duration = 1000
        }
        val loginAnim = ObjectAnimator.ofFloat(findViewById<Button>
            (R.id.button_login), View.ALPHA, 1f).apply {
            duration = 1000
        }
        val buttonSet = AnimatorSet().apply {
            playTogether(registerAnim, loginAnim)
        }
        AnimatorSet().apply {
            playSequentially(bannerAnim, welcomeAnim, descriptionAnim, buttonSet)
            start()
        }
    }
}
