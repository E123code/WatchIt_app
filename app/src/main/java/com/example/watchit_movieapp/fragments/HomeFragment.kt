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
import com.example.watchit_movieapp.adapters.MediaAdapter
import com.example.watchit_movieapp.databinding.HomeFragmentBinding
import com.example.watchit_movieapp.interfaces.MediaItemClickedCallback
import com.example.watchit_movieapp.interfaces.TitleCallback
import com.example.watchit_movieapp.model.MediaItem
import com.example.watchit_movieapp.utilities.AdapterMode
import com.example.watchit_movieapp.utilities.Constants
import com.example.watchit_movieapp.utilities.FireStoreManager
import com.example.watchit_movieapp.utilities.ImageLoader
import com.example.watchit_movieapp.utilities.RetrofitClient
import com.example.watchit_movieapp.utilities.SignalManager
import com.example.watchit_movieapp.utilities.openDetails
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: HomeFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var mediaAdapter: MediaAdapter

    private var cachedMovies: List<MediaItem>? = null
    private var cachedTVShows: List<MediaItem>? = null
    private var lastLoadedUrl = ""


    private val userObserver: (com.example.watchit_movieapp.model.User?) -> Unit = { _ ->
        if (isAdded && !isHidden) {
            refreshUI()
        }
    }


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

        updateProfileImageUI()

        binding.BTNSearch.setOnClickListener {
            (activity as? MainActivity)?.navigateToSearch()
        }

        setupTabsLogic()
        FireStoreManager.getInstance().addObserver(userObserver)
    }

    private fun updateProfileImageUI() {
        val user = FireStoreManager.getInstance().currentUser
        if (user != null && user.profileImageUrl.isNotEmpty() && user.profileImageUrl != lastLoadedUrl) {
            ImageLoader.getInstance().loadProfile(user.profileImageUrl, binding.ProfileIMG)
            lastLoadedUrl = user.profileImageUrl
        }
    }

    /*   private fun applyCurrentFavorites(items: List<MediaItem>) {
           val favorites = FireStoreManager.getInstance().currentUser?.favorites ?: emptyList()
           items.forEach { it.isFavorite = favorites.contains(it.id) }
       }*/


    private fun setupRecyclerView() {
        val callback = object : MediaItemClickedCallback {
            override fun mediaItemClicked(mediaItem: MediaItem) {

                openDetails(mediaItem)
            }
        }
        mediaAdapter = MediaAdapter(emptyList(), AdapterMode.HOME, callback)

        mediaAdapter.titleCallback = object : TitleCallback {
            override fun favoriteButtonClicked(title: MediaItem, position: Int) {
                val viewHolder =
                    binding.rvMedia.findViewHolderForAdapterPosition(position) as? MediaAdapter.MediaViewHolder
                viewHolder?.binding?.IMGFavorite?.isEnabled = false

                val previous = title.isFavorite
                title.toggleFavorite()
                mediaAdapter.notifyItemChanged(position)
                SignalManager.getInstance().vibrate()

                if (title.isFavorite) {
                    FireStoreManager.getInstance()
                        .addTitle(title, Constants.FIRESTORE.FAVORITES) { result ->
                            viewHolder?.binding?.IMGFavorite?.isEnabled = true // שחרור הנעילה
                            if (result != Constants.logMessage.SUCCESS) {
                                title.isFavorite = previous
                                mediaAdapter.notifyItemChanged(position)
                                SignalManager.getInstance()
                                    .toast(" error", SignalManager.ToastLength.SHORT)
                            } else {
                                SignalManager.getInstance()
                                    .toast("added to favorites", SignalManager.ToastLength.SHORT)
                            }
                        }
                } else {
                    FireStoreManager.getInstance()
                        .removeTitle(title.id, Constants.FIRESTORE.FAVORITES) { success ->
                            viewHolder?.binding?.IMGFavorite?.isEnabled = true // שחרור הנעילה
                            if (!success) {
                                title.isFavorite = previous
                                mediaAdapter.notifyItemChanged(position)
                                SignalManager.getInstance()
                                    .toast(
                                        "deleted from favorites",
                                        SignalManager.ToastLength.SHORT
                                    )
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
        binding.homeTabLayout.addOnTabSelectedListener(object :
            com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
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
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getPopularMovies()

                if (response.results.isNotEmpty()) {
                    response.results.forEach {
                        it.mediaType = "movie"
                    }
//                    applyCurrentFavorites(response.results)
                    cachedMovies = response.results
                    mediaAdapter.updateData(response.results)
                }
            } catch (e: Exception) {
                Log.e(Constants.TAGS.HOME_TAG, "Error fetching movies: ${e.message}")
                SignalManager.getInstance()
                    .toast("Failed to load data", SignalManager.ToastLength.SHORT)
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
                    }
//                    applyCurrentFavorites(response.results)
                    cachedTVShows = response.results
                    mediaAdapter.updateData(response.results)
                }
            } catch (e: Exception) {
                Log.e(Constants.TAGS.HOME_TAG, "Error fetching tv shows: ${e.message}")
                SignalManager.getInstance()
                    .toast("Failed to load data", SignalManager.ToastLength.SHORT)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isHidden) {
            Log.d(Constants.TAGS.HOME_TAG, "User returned to Home tab")
            refreshUI()
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            Log.d(Constants.TAGS.HOME_TAG, "User returned to Home tab")
            refreshUI()
        }

    }

    private fun refreshUI() {
        if (::mediaAdapter.isInitialized) {
            mediaAdapter.syncFavorites()
            mediaAdapter.notifyDataSetChanged()
            updateProfileImageUI()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        FireStoreManager.getInstance().removeObserver(userObserver)
        _binding = null
    }

}