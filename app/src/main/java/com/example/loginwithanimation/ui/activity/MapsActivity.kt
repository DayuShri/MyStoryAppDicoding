package com.example.loginwithanimation.ui.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.loginwithanimation.R
import com.example.loginwithanimation.data.response.StoryItem
import com.example.loginwithanimation.ui.model.StoryViewModel
import com.example.loginwithanimation.ui.model.ViewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private val storyViewModel: StoryViewModel by viewModels {
        ViewModelFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        loadStoriesWithLocation()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true
        Log.d("MapsActivity", "Google Map is ready!")
    }

    private fun loadStoriesWithLocation() {
        lifecycleScope.launch {
            val token = intent.getStringExtra("TOKEN_KEY")
            if (token.isNullOrEmpty()) {
                Toast.makeText(this@MapsActivity, "Token tidak ditemukan!", Toast.LENGTH_SHORT).show()
                return@launch
            }

            storyViewModel.fetchStoriesWithLocation(token).observe(this@MapsActivity) { stories ->
                if (!stories.isNullOrEmpty()) {
                    Log.d("MapsActivity", "${stories.size} stories loaded")
                    addMarkersToMap(stories)
                } else {
                    Toast.makeText(this@MapsActivity, "Tidak ada cerita dengan lokasi.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun addMarkersToMap(stories: List<StoryItem>) {
        googleMap.clear()

        var firstMarkerPosition: LatLng? = null

        stories.forEach { story ->
            val lat = story.lat
            val lon = story.lon

            if (lat != null && lon != null) {
                val position = LatLng(lat, lon)
                googleMap.addMarker(
                    MarkerOptions()
                        .position(position)
                        .title(story.name)
                        .snippet(story.description)
                )

                if (firstMarkerPosition == null) {
                    firstMarkerPosition = position
                }
            } else {
                Log.d("MapsActivity", "Invalid location for story: ${story.name}")
            }
        }

        firstMarkerPosition?.let {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 10f))
        } ?: run {
            Toast.makeText(this, "Tidak ada lokasi valid untuk ditampilkan.", Toast.LENGTH_SHORT).show()
        }
    }
}
