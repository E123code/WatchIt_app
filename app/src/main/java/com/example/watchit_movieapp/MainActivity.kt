package com.example.watchit_movieapp

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.watchit_movieapp.Model.MediaItem
import com.example.watchit_movieapp.adapters.MediaAdapter
import com.example.watchit_movieapp.databinding.ActivityMainBinding
import com.example.watchit_movieapp.interfaces.FavoriteCallback
import com.example.watchit_movieapp.utilities.RetrofitClient
import com.example.watchit_movieapp.utilities.SignalManager
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mediaAdapter: MediaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        loadMovies()

    }

    private fun setupRecyclerView() {
        mediaAdapter = MediaAdapter(emptyList(), isSearchMode = false)

        mediaAdapter.favoriteCallback = object : FavoriteCallback {
            override fun favoriteButtonClicked(movie: MediaItem, position: Int) {
                // שימוש בפונקציית ה-toggle שבנית במודל
                movie.toggleFavorite()

                // עדכון ה-UI בשורה הספציפית
                mediaAdapter.notifyItemChanged(position)
                Log.d("TEST", "4. Adapter updated")
            }
        }
        binding.RVMovieList.adapter = mediaAdapter
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = RecyclerView.VERTICAL
        binding.RVMovieList.layoutManager = linearLayoutManager


        }

    private fun loadMovies() {
        // שימוש ב-lifecycleScope כדי להריץ את ה-Retrofit ברקע
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getPopularMovies()

                if (response.results.isNotEmpty()) {
                    // עדכון האדאפטר בנתונים האמיתיים
                    Log.d("TEST", "Movies count: ${response.results.size}")
                    mediaAdapter.updateData(response.results)
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error fetching movies: ${e.message}")
                SignalManager.getInstance().toast("Failed to load data", SignalManager.ToastLength.SHORT)
            }
        }
    }

}