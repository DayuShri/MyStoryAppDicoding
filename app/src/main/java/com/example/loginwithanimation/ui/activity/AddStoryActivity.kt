package com.example.loginwithanimation.ui.activity

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.loginwithanimation.R
import com.example.loginwithanimation.data.preferences.SessionPreferences
import com.example.loginwithanimation.ui.model.StoryViewModel
import com.example.loginwithanimation.ui.model.ViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.*

class AddStoryActivity : AppCompatActivity() {

    private lateinit var previewImageView: ImageView
    private lateinit var openGalleryLauncher: ActivityResultLauncher<Intent>
    private var selectedImageUri: Uri? = null
    private val storyViewModel: StoryViewModel by viewModels { ViewModelFactory(this) }
    private lateinit var sessionPreferences: SessionPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_story)

        sessionPreferences = SessionPreferences(this)

        previewImageView = findViewById(R.id.iv_preview)
        val progressIndicator = findViewById<ProgressBar>(R.id.add_story_progress_bar)
        val descriptionEditText = findViewById<EditText>(R.id.ed_add_description)

        openGalleryLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val imageUri = result.data?.data
                if (imageUri != null) {
                    selectedImageUri = imageUri
                    previewImageView.setImageURI(imageUri)
                } else {
                    Toast.makeText(this, R.string.failed_to_load_image, Toast.LENGTH_SHORT).show()
                }
            }
        }

        findViewById<Button>(R.id.button_gallery).setOnClickListener { openGallery() }
        findViewById<Button>(R.id.button_add).setOnClickListener {
            val description = descriptionEditText.text.toString().trim()
            if (selectedImageUri != null && description.isNotEmpty()) {
                getTokenAndUpload(description, selectedImageUri!!, progressIndicator)
            } else {
                Toast.makeText(this, R.string.incomplete_upload_story, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK).setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        openGalleryLauncher.launch(intent)
    }

    private suspend fun compressImage(file: File): File {
        return Compressor.compress(this, file) {
            resolution(1280, 720)
            quality(80)
            format(Bitmap.CompressFormat.JPEG)
            size(1_000_000)
        }
    }

    private fun uriToFile(uri: Uri): File {
        val contentResolver = contentResolver
        val tempFile = File.createTempFile("temp_image", ".jpg", cacheDir)
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                tempFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return tempFile
    }

    private fun getTokenAndUpload(description: String, imageUri: Uri, progressIndicator: ProgressBar) {
        lifecycleScope.launch {
            val token = sessionPreferences.token.first()
            if (token.isNullOrEmpty()) {
                Toast.makeText(this@AddStoryActivity, "Token tidak ditemukan!", Toast.LENGTH_SHORT).show()
                return@launch
            }

            uploadStory(token, description, imageUri, progressIndicator)
        }
    }

    private fun uploadStory(token: String, description: String, imageUri: Uri, progressIndicator: ProgressBar) {
        progressIndicator.visibility = View.VISIBLE

        val file = uriToFile(imageUri)
        lifecycleScope.launch {
            val compressedFile = compressImage(file)

            val descriptionPart = description.toRequestBody("text/plain".toMediaType())
            val filePart = MultipartBody.Part.createFormData(
                "photo",
                compressedFile.name,
                compressedFile.asRequestBody("image/jpeg".toMediaType())
            )

            storyViewModel.addStory(token, descriptionPart, filePart).observe(this@AddStoryActivity) { response ->
                if (!response.error) {
                    Toast.makeText(this@AddStoryActivity, response.message, Toast.LENGTH_SHORT).show()

                    lifecycleScope.launch {
                        delay(2000)
                        // Inform StoryActivity to refresh the list
                        setResult(RESULT_OK, Intent().putExtra("NEW_STORY_ADDED", true))
                        finish()
                    }
                } else {
                    Toast.makeText(this@AddStoryActivity, response.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("selectedImageUri", selectedImageUri)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        selectedImageUri = savedInstanceState.getParcelable("selectedImageUri")
        selectedImageUri?.let { previewImageView.setImageURI(it) }
    }
}