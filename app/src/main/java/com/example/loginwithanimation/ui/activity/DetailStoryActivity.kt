package com.example.loginwithanimation.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.loginwithanimation.R
import com.example.loginwithanimation.data.preferences.SessionPreferences
import com.example.loginwithanimation.ui.model.StoryViewModel
import com.example.loginwithanimation.ui.model.ViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DetailStoryActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_STORY_ID = "STORY_ID"
        private const val KEY_NAME = "KEY_NAME"
        private const val KEY_DESCRIPTION = "KEY_DESCRIPTION"
        private const val KEY_PHOTO_URL = "KEY_PHOTO_URL"
    }

    private lateinit var photoImageView: ImageView
    private lateinit var nameTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var progressBar: ProgressBar

    private val storyViewModel: StoryViewModel by viewModels {
        ViewModelFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_story)

        photoImageView = findViewById(R.id.iv_detail_photo)
        nameTextView = findViewById(R.id.tv_detail_name)
        descriptionTextView = findViewById(R.id.tv_detail_description)
        progressBar = findViewById(R.id.detail_progress_bar)

        if (savedInstanceState != null) {
            val name = savedInstanceState.getString(KEY_NAME)
            val description = savedInstanceState.getString(KEY_DESCRIPTION)
            val photoUrl = savedInstanceState.getString(KEY_PHOTO_URL)

            if (name != null && description != null && photoUrl != null) {
                updateUI(name, description, photoUrl)
                return
            }
        }

        val storyId = intent.getStringExtra(EXTRA_STORY_ID)
        if (storyId == null) {
            Toast.makeText(this, "Cerita tidak ditemukan!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            val token = SessionPreferences(this@DetailStoryActivity).token.first()
            if (token.isNullOrEmpty()) {
                Toast.makeText(this@DetailStoryActivity, "Token tidak ditemukan!", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }

            loadStoryDetail(token, storyId)
        }
    }

    private fun loadStoryDetail(token: String, storyId: String) {
        progressBar.visibility = View.VISIBLE

        storyViewModel.getStoryDetail(token, storyId).observe(this) { response ->
            progressBar.visibility = View.GONE
            if (response != null && !response.error) {
                updateUI(response.story.name, response.story.description, response.story.photoUrl)
            } else {
                Toast.makeText(this, response?.message ?: "Gagal memuat detail cerita", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUI(name: String, description: String, photoUrl: String) {
        nameTextView.text = name
        descriptionTextView.text = description

        Glide.with(this)
            .load(photoUrl)
            .placeholder(R.drawable.ic_place_holder)
            .error(R.drawable.ic_place_holder)
            .into(photoImageView)
        photoImageView.tag = photoUrl
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_NAME, nameTextView.text.toString())
        outState.putString(KEY_DESCRIPTION, descriptionTextView.text.toString())
        outState.putString(KEY_PHOTO_URL, photoImageView.tag as? String)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val name = savedInstanceState.getString(KEY_NAME)
        val description = savedInstanceState.getString(KEY_DESCRIPTION)
        val photoUrl = savedInstanceState.getString(KEY_PHOTO_URL)

        if (name != null && description != null && photoUrl != null) {
            updateUI(name, description, photoUrl)
        }
    }
}
