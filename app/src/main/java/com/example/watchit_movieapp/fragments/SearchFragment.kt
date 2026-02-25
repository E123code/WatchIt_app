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
import com.example.watchit_movieapp.interfaces.FavoriteCallback
import com.example.watchit_movieapp.interfaces.MediaItemClickedCallback
import com.example.watchit_movieapp.model.MediaItem
import com.example.watchit_movieapp.utilities.GenresMap
import com.example.watchit_movieapp.utilities.RetrofitClient
import com.example.watchit_movieapp.utilities.SignalManager
import com.example.watchit_movieapp.utilities.openDetails
import kotlinx.coroutines.launch


class SearchFragment : Fragment() {

    private lateinit var binding: SearchFragmentBinding

    private lateinit var mediaAdapter: MediaAdapter

    private var resultsList: List<MediaItem> = emptyList()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = SearchFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setupRecyclerView()
        setupSearchAndFilters()

    }


    private fun setupRecyclerView() {
        val callback = object : MediaItemClickedCallback {
            override fun mediaItemClicked(mediaItem: MediaItem) {
                openDetails(mediaItem)
            }
        }
        mediaAdapter = MediaAdapter(emptyList(), isSearchMode = false, callback)

        mediaAdapter.favoriteCallback = object : FavoriteCallback {
            override fun favoriteButtonClicked(title: MediaItem, position: Int) {
                // שימוש בפונקציית ה-toggle שבנית במודל
                title.toggleFavorite()

                // עדכון ה-UI בשורה הספציפית
                mediaAdapter.notifyItemChanged(position)
                Log.d("TEST", "4. Adapter updated")
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
                    performSearch(query) // פונקציה שתביא נתונים מה-API
                    binding.searchBar.clearFocus()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean = true
        })


        // 2. סינון לפי צ'יפים
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
                val chipText = chip.text.toString()

                // search by text
                when (chipText) {
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


}