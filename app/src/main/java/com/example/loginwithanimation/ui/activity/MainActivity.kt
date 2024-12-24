package com.example.loginwithanimation.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.loginwithanimation.R
import com.example.loginwithanimation.data.response.StoryItem
import com.example.loginwithanimation.data.preferences.SessionPreferences
import com.example.loginwithanimation.ui.adapter.StoryAdapter
import com.example.loginwithanimation.ui.adapter.StoryLoadStateAdapter
import com.example.loginwithanimation.ui.model.StoryViewModel
import com.example.loginwithanimation.ui.model.ViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val storyViewModel: StoryViewModel by viewModels { ViewModelFactory(this) }
    private lateinit var sessionPreferences: SessionPreferences
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var storyAdapter: StoryAdapter

    private val addStoryResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val isNewStoryAdded = result.data?.getBooleanExtra("NEW_STORY_ADDED", false) ?: false
            if (isNewStoryAdded) {
                loadStoriesWithPaging()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        sessionPreferences = SessionPreferences(this)

        setupToolbar()
        setupRecyclerView()
        setupFab()

        progressBar = findViewById(R.id.story_progress_bar)

        observePagingState()
        loadStoriesWithPaging()
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.list_story)
        recyclerView.layoutManager = LinearLayoutManager(this)
        storyAdapter = StoryAdapter { story -> navigateToDetail(story) }
        recyclerView.adapter = storyAdapter.withLoadStateFooter(
            footer = StoryLoadStateAdapter { storyAdapter.retry() }
        )
    }

    private fun setupFab() {
        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this, AddStoryActivity::class.java)
            addStoryResultLauncher.launch(intent)
        }
    }


    private fun loadStoriesWithPaging() {
        lifecycleScope.launch {
            val token = sessionPreferences.token.first()
            if (token.isNullOrEmpty()) {
                Toast.makeText(this@MainActivity, "Token not found!", Toast.LENGTH_SHORT).show()
                return@launch
            }

            storyViewModel.getStoriesPaging(token).collectLatest { pagingData ->
                storyAdapter.submitData(pagingData)
            }
        }
    }

    private fun observePagingState() {
        storyViewModel.pagingState.observe(this) { needRefresh ->
            if (needRefresh) {
                refreshStories()
                storyViewModel.resetPagingState()
            }
        }
    }

    private fun refreshStories() {
        storyAdapter.refresh()
    }

    private fun navigateToDetail(story: StoryItem) {
        val intent = Intent(this, DetailStoryActivity::class.java).apply {
            putExtra(DetailStoryActivity.EXTRA_STORY_ID, story.id)
        }
        startActivity(intent)
    }

    private fun setupToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Dicoding Story"
        toolbar.setTitleTextColor(resources.getColor(R.color.white, theme))
        toolbar.overflowIcon?.setTint(resources.getColor(R.color.white, theme))

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_location -> {
                navigateToMaps()
                true
            }
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun navigateToMaps() {
        lifecycleScope.launch {
            val token = sessionPreferences.token.first()
            if (!token.isNullOrEmpty()) {
                val intent = Intent(this@MainActivity, MapsActivity::class.java)
                intent.putExtra("TOKEN_KEY", token)
                startActivity(intent)
            } else {
                Toast.makeText(this@MainActivity, "Token not found!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun logout() {
        lifecycleScope.launch {
            sessionPreferences.clearSession()
            startActivity(Intent(this@MainActivity, WelcomeActivity::class.java))
            finish()
        }
    }
}
