package com.example.watchit_movieapp.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.watchit_movieapp.MainActivity
import com.example.watchit_movieapp.Model.MediaItem
import com.example.watchit_movieapp.adapters.MediaAdapter
import com.example.watchit_movieapp.databinding.HomeFragmentBinding
import com.example.watchit_movieapp.interfaces.FavoriteCallback
import com.example.watchit_movieapp.utilities.RetrofitClient
import com.example.watchit_movieapp.utilities.SignalManager
import kotlinx.coroutines.launch

class HomeFragment: Fragment() {

    private var _binding: HomeFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var mediaAdapter: MediaAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = HomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setupRecyclerView()


        loadMovies()


        binding.BTNSearch.setOnClickListener {
            (activity as? MainActivity)?.navigateToSearch()
        }


        setupTabsLogic()
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

        binding.rvMedia.adapter = mediaAdapter
        binding.rvMedia.layoutManager = LinearLayoutManager(requireContext())


    }



    private fun setupTabsLogic() {
        binding.homeTabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> loadMovies()    // טאב Movies
                    1 -> loadTVShows()   // טאב TV Shows
                }
            }

            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
    }

    private fun loadMovies() {
        // שימוש ב-lifecycleScope כדי להריץ את ה-Retrofit ברקע
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getPopularMovies()

                if (response.results.isNotEmpty()) {
                    mediaAdapter.updateData(response.results)
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error fetching movies: ${e.message}")
                SignalManager.getInstance().toast("Failed to load data", SignalManager.ToastLength.SHORT)
            }
        }
    }

    private fun loadTVShows() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getPopularTVShows()

                if (response.results.isNotEmpty()) {
                    mediaAdapter.updateData(response.results)
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error fetching tvshows: ${e.message}")
                SignalManager.getInstance().toast("Failed to load data", SignalManager.ToastLength.SHORT)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // חשוב למניעת זליגת זיכרון
    }

}