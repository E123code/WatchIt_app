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
import com.example.watchit_movieapp.model.MediaItem
import com.example.watchit_movieapp.adapters.MediaAdapter
import com.example.watchit_movieapp.databinding.HomeFragmentBinding
import com.example.watchit_movieapp.interfaces.TitleCallback
import com.example.watchit_movieapp.interfaces.MediaItemClickedCallback
import com.example.watchit_movieapp.utilities.AdapterMode
import com.example.watchit_movieapp.utilities.Constants
import com.example.watchit_movieapp.utilities.FireStoreManager
import com.example.watchit_movieapp.utilities.ImageLoader
import com.example.watchit_movieapp.utilities.RetrofitClient
import com.example.watchit_movieapp.utilities.SignalManager
import com.example.watchit_movieapp.utilities.openDetails
import kotlinx.coroutines.launch

class HomeFragment: Fragment() {

    private var _binding: HomeFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var mediaAdapter: MediaAdapter

    private var cachedMovies: List<MediaItem>? = null
    private var cachedTVShows: List<MediaItem>? = null





    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = HomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateProfileImageUI()

        setupRecyclerView()
        loadMovies()

        binding.BTNSearch.setOnClickListener {
            (activity as? MainActivity)?.navigateToSearch()
        }

        setupTabsLogic()
    }

    private fun updateProfileImageUI() {
        val user = FireStoreManager.currentUser
        if (user != null && user.profileImageUrl.isNotEmpty()) {
            ImageLoader.getInstance().loadProfile(user.profileImageUrl, binding.ProfileIMG)
        }
    }




    override fun onResume() {
        super.onResume()
        if (!isHidden) {
            Log.d(Constants.logMessage.HOME_KEY, "User returned to Home tab")
            refreshHeartsUI()
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            Log.d(Constants.logMessage.HOME_KEY, "User returned to Home tab")
            updateProfileImageUI()
            refreshHeartsUI()
        }

    }

    private fun refreshHeartsUI() {
        cachedMovies?.forEach { it.isFavorite = FireStoreManager.isInFavorites(it.id) }
        cachedTVShows?.forEach { it.isFavorite = FireStoreManager.isInFavorites(it.id) }

        if (binding.homeTabLayout.selectedTabPosition == 0) {
            cachedMovies?.let { mediaAdapter.updateData(it) }
        } else {
            cachedTVShows?.let { mediaAdapter.updateData(it) }
        }
    }

    private fun setupRecyclerView() {
        val callback = object : MediaItemClickedCallback {
            override fun mediaItemClicked(mediaItem: MediaItem) {

                openDetails(mediaItem)
            }
        }
        mediaAdapter = MediaAdapter(emptyList(), AdapterMode.HOME,callback)

        mediaAdapter.titleCallback = object : TitleCallback {
            override fun favoriteButtonClicked(title: MediaItem, position: Int) {
                val previous = title.isFavorite
                title.toggleFavorite()
                SignalManager.getInstance().vibrate()
                mediaAdapter.notifyItemChanged(position)

                if (title.isFavorite) {
                    FireStoreManager.addFavorite(title) { success ->
                        if (!success) {
                            title.isFavorite = previous
                            mediaAdapter.notifyItemChanged(position)
                            SignalManager.getInstance()
                                .toast("Connection error", SignalManager.ToastLength.SHORT)
                        } else {
                            SignalManager.getInstance()
                                .toast("added to favorites", SignalManager.ToastLength.SHORT)
                        }

                    }
                } else {
                    FireStoreManager.deleteFavorite(title.id) { success ->
                        if (!success) {
                            title.isFavorite = previous
                            mediaAdapter.notifyItemChanged(position)
                        } else {
                            SignalManager.getInstance()
                                .toast("deleted from favorites", SignalManager.ToastLength.SHORT)
                        }
                    }
                }
            }

            override fun deleteButtonClicked(
                title: MediaItem,
                position: Int
            ) {

            }
        }

        binding.rvMedia.adapter = mediaAdapter
        binding.rvMedia.layoutManager = LinearLayoutManager(requireContext())


    }



    private fun setupTabsLogic() {
        binding.homeTabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> loadMovies()
                    1 -> loadTVShows()
                }
            }

            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
    }

    private fun loadMovies() {
        if (cachedMovies != null) {
            mediaAdapter.updateData(cachedMovies!!)
            return
        }
        // שימוש ב-lifecycleScope כדי להריץ את ה-Retrofit ברקע
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getPopularMovies()

                if (response.results.isNotEmpty()) {
                    response.results.forEach {
                        it.mediaType = "movie"
                        it.isFavorite = FireStoreManager.isInFavorites(it.id)
                    }
                    cachedMovies = response.results
                    mediaAdapter.updateData(response.results)
                }
            } catch (e: Exception) {
                Log.e("home fragment", "Error fetching movies: ${e.message}")
                SignalManager.getInstance().toast("Failed to load data", SignalManager.ToastLength.SHORT)
            }
        }
    }

    private fun loadTVShows() {
        if (cachedTVShows != null) {
            mediaAdapter.updateData(cachedTVShows!!)
            return
        }
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getPopularTVShows()

                if (response.results.isNotEmpty()) {
                    response.results.forEach {
                        it.mediaType = "tv"
                        it.isFavorite = FireStoreManager.isInFavorites(it.id)
                    }
                    cachedTVShows = response.results
                    mediaAdapter.updateData(response.results)
                }
            } catch (e: Exception) {
                Log.e("home fragment", "Error fetching tv shows: ${e.message}")
                SignalManager.getInstance().toast("Failed to load data", SignalManager.ToastLength.SHORT)
            }
        }
    }





    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}