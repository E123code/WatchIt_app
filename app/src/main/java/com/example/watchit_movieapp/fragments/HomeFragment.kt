package com.example.watchit_movieapp.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.watchit_movieapp.DetailsActivity
import com.example.watchit_movieapp.MainActivity
import com.example.watchit_movieapp.model.MediaItem
import com.example.watchit_movieapp.adapters.MediaAdapter
import com.example.watchit_movieapp.databinding.HomeFragmentBinding
import com.example.watchit_movieapp.interfaces.FavoriteCallback
import com.example.watchit_movieapp.interfaces.MediaItemClickedCallback
import com.example.watchit_movieapp.utilities.Constants
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
        val callback = object : MediaItemClickedCallback {
            override fun mediaItemClicked(mediaItem: MediaItem) {
                // קריאה לפונקציה שכתבת למטה
                openDetails(mediaItem)
            }
        }
        mediaAdapter = MediaAdapter(emptyList(), isSearchMode = false,callback)

        mediaAdapter.favoriteCallback = object : FavoriteCallback {
            override fun favoriteButtonClicked(title: MediaItem, position: Int) {
                // שימוש בפונקציית ה-toggle שבנית במודל
                title.toggleFavorite()

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
                    response.results.forEach { it.mediaType = "movie" }
                    mediaAdapter.updateData(response.results)
                }
            } catch (e: Exception) {
                Log.e("home fragment", "Error fetching movies: ${e.message}")
                SignalManager.getInstance().toast("Failed to load data", SignalManager.ToastLength.SHORT)
            }
        }
    }

    private fun loadTVShows() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getPopularTVShows()

                if (response.results.isNotEmpty()) {
                    response.results.forEach { it.mediaType = "tv" }
                    mediaAdapter.updateData(response.results)
                }
            } catch (e: Exception) {
                Log.e("home fragment", "Error fetching tv shows: ${e.message}")
                SignalManager.getInstance().toast("Failed to load data", SignalManager.ToastLength.SHORT)
            }
        }
    }

    private fun openDetails(item: MediaItem) {
        val intent = Intent(requireContext(), DetailsActivity::class.java)
        var bundle = Bundle()
        bundle.putInt(Constants.bundlekeys.ID_KEY, item.id)
        bundle.putString(Constants.bundlekeys.TYPE_KEY, item.mediaType) // "movie" או "tv" שהזרקנו ב-loadMovies/TV
        intent.putExtras(bundle)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // חשוב למניעת זליגת זיכרון
    }

}