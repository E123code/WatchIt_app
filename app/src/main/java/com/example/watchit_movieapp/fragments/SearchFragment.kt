package com.example.watchit_movieapp.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.watchit_movieapp.R
import com.example.watchit_movieapp.adapters.MediaAdapter
import com.example.watchit_movieapp.databinding.SearchFragmentBinding
import com.example.watchit_movieapp.interfaces.TitleCallback
import com.example.watchit_movieapp.interfaces.MediaItemClickedCallback
import com.example.watchit_movieapp.model.MediaItem
import com.example.watchit_movieapp.utilities.AdapterMode
import com.example.watchit_movieapp.utilities.FireStoreManager
import com.example.watchit_movieapp.utilities.GenresMap
import com.example.watchit_movieapp.utilities.RetrofitClient
import com.example.watchit_movieapp.utilities.SignalManager
import com.example.watchit_movieapp.utilities.openDetails
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch


class SearchFragment : Fragment() {

    private var resultsList: List<MediaItem> = emptyList()

    private var _binding: SearchFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var mediaAdapter: MediaAdapter

    private var favoritesListener: ListenerRegistration? = null
    private var currentFavoriteIds: List<String> = emptyList()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = SearchFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()



        setupSearchAndFilters()

        if (resultsList.isNotEmpty()) {
            mediaAdapter.updateData(resultsList)
            binding.results.visibility = View.VISIBLE
            binding.genresScrollView.visibility = View.VISIBLE

        }

    }


    private fun setupRecyclerView() {
        val callback = object : MediaItemClickedCallback {
            override fun mediaItemClicked(mediaItem: MediaItem) {
                openDetails(mediaItem)
            }
        }
        mediaAdapter = MediaAdapter(emptyList(), AdapterMode.NATURAL,callback=callback)

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
                item: MediaItem,
                position: Int
            ) {

            }
        }

        binding.results.adapter = mediaAdapter
        binding.results.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupSearchAndFilters() {
        // 1. חיפוש טקסט
        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    performSearch(query)
                    binding.searchBar.clearFocus()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    resultsList = emptyList()
                    mediaAdapter.updateData(emptyList())
                    binding.genresScrollView.visibility = View.GONE
                    binding.chipGroupGenres.clearCheck()
                }
                return true
            }
        })



        binding.chipGroupGenres.setOnCheckedStateChangeListener { _, checkedIds ->
            applyFilters(checkedIds)
        }
    }

    private fun performSearch(query: String) {
        lifecycleScope.launch {
            try {

                val response = RetrofitClient.searchByName(query)

                resultsList =
                    response.results.filter { mediaItem -> mediaItem.mediaType == "tv" || mediaItem.mediaType == "movie" }

                resultsList.forEach { it.isFavorite = currentFavoriteIds.contains(it.id) }

                binding.genresScrollView.visibility = View.VISIBLE
                binding.chipGroupGenres.clearCheck()

                if (resultsList.isEmpty()) {
                    binding.results.visibility = View.GONE
                    binding.noResultsNotify.visibility = View.VISIBLE
                } else {
                    binding.results.visibility = View.VISIBLE
                    binding.noResultsNotify.visibility = View.GONE
                    mediaAdapter.updateData(resultsList)
                }


            } catch (e: Exception) {
                Log.e("search", "Error: ${e.message}")
                SignalManager.getInstance()
                    .toast("Failed to load data", SignalManager.ToastLength.SHORT)
            }
        }
    }

    private fun applyFilters(checkedIds: List<Int>) {
        if (checkedIds.isEmpty()) {
            mediaAdapter.updateData(resultsList)
            return
        }

        val filteredList = resultsList.filter { item ->
            var matches = true //the condition

            checkedIds.forEach { chipId ->
                // gets the chip by his ID
                val chip =
                    binding.chipGroupGenres.findViewById<com.google.android.material.chip.Chip>(
                        chipId
                    )
                // search by text
                when (val chipText = chip.text.toString()) {
                    getString(R.string.movies) -> if (item.mediaType != "movie") matches = false
                    getString(R.string.tv_shows) -> if (item.mediaType != "tv") matches = false

                    else -> {
                        val validIds = GenresMap.getIdsByKeyword(chipText)
                        val hasGenres = item.genreIds?.any { it in validIds } ?: false
                        if (!hasGenres ) {
                            matches = false
                        }
                    }
                }
            }
            matches
        }
        mediaAdapter.updateData(filteredList)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            stopFavoritesListen()
        }else{
            startFavoritesListen()
        }
    }


    private fun startFavoritesListen(){
        if (favoritesListener != null ) return

        favoritesListener = FireStoreManager.loadCurrentUser { user ->
            currentFavoriteIds = user.favorites

            resultsList.forEach { it.isFavorite = currentFavoriteIds.contains(it.id) }

            if (::mediaAdapter.isInitialized) {
                mediaAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun stopFavoritesListen() {
        favoritesListener?.remove()
        favoritesListener = null
    }

    override fun onResume() {
        super.onResume()

        if (!isHidden) {
            startFavoritesListen()
        }
    }





    override fun onPause() {
        super.onPause()
        stopFavoritesListen()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        favoritesListener?.remove()
        _binding = null
    }


}