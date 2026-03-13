package com.example.watchit_movieapp

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.watchit_movieapp.adapters.CastAdapter
import com.example.watchit_movieapp.adapters.ListSelectAdapter
import com.example.watchit_movieapp.adapters.ProviderAdapter
import com.example.watchit_movieapp.databinding.ActivityDetailsBinding
import com.example.watchit_movieapp.databinding.DialogWatchlistsBinding
import com.example.watchit_movieapp.interfaces.AddCallback
import com.example.watchit_movieapp.model.ProviderItem
import com.example.watchit_movieapp.model.TitleDetails
import com.example.watchit_movieapp.model.Watchlist
import com.example.watchit_movieapp.utilities.Constants
import com.example.watchit_movieapp.utilities.FireStoreManager
import com.example.watchit_movieapp.utilities.ImageLoader
import com.example.watchit_movieapp.utilities.RetrofitClient
import com.example.watchit_movieapp.utilities.SignalManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch


class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding

    private lateinit var castAdapter: CastAdapter
    private lateinit var providerAdapter: ProviderAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
    }


    private fun initViews() {
        val bundle: Bundle? = intent.extras
        val id = bundle?.getInt(Constants.bundlekeys.ID_KEY) ?: -1
        val type = bundle?.getString(Constants.bundlekeys.TYPE_KEY) ?: "movie"

        Log.d(Constants.logMessage.DETAILS_KEY, "Received ID: $id")
        Log.d(Constants.logMessage.DETAILS_KEY, "Received Type: $type")

        setupRecyclerViews()

        binding.BTNBack.setOnClickListener {
            finish()
        }

        if (id != -1) {
            fetchDetails(id, type)
        }
    }

    private fun setupRecyclerViews() {
        //Actors List
        castAdapter = CastAdapter()
        binding.rvCast.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvCast.adapter = castAdapter

        // Providers List
        providerAdapter = ProviderAdapter()
        binding.rvProviders.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvProviders.adapter = providerAdapter
    }

    private fun fetchDetails(Id: Int, type: String) {
        lifecycleScope.launch {
            try {

                val response = RetrofitClient.getDetails(Id, type)

                response.mediaType = type

                updateUI(response)
            } catch (e: Exception) {
                Log.e("Details", "Error: ${e.message}")
                SignalManager.getInstance()
                    .toast("Failed to load data", SignalManager.ToastLength.SHORT)
            }
        }


    }


    private fun updateUI(details: TitleDetails) {
        val id = details.id.toString()
        binding.Title.text = details.name
        binding.ageRating.text = details.ageRating
        binding.releaseYearDurationLine.text = "${details.date} • ${details.duration}"
        binding.genresDetails.text = details.genres
        binding.ratingBar.rating = (details.rating / 2).toFloat()

        FireStoreManager.getInstance().getUserRating(id) { rating ->
            binding.personalRate.rating = rating ?: 0f
        }

        binding.personalRate.setOnRatingBarChangeListener { _, rating, fromUser ->
            if (fromUser) {
                FireStoreManager.getInstance().saveUserRating(id, rating) { success ->
                    if (success) SignalManager.getInstance()
                        .toast("Rated: $rating", SignalManager.ToastLength.SHORT)
                }
            }

        }

        details.isFavorite = FireStoreManager.getInstance().isInFavorites(id)
        updateHeartUI(details.isFavorite)

        binding.BTNFavorite.setOnClickListener {
            favoriteClick(details)
        }


        val providers = details.watchProviders?.results?.get("IL")?.membership
        updateProviders(providers)

        binding.Overview.text = details.overview

        ImageLoader.getInstance().loadImage(details.fullPosterUrl, binding.IMGPoster)
        castAdapter.updateData(details.credits?.cast)

        binding.BTNAddList.setOnClickListener {
            showWatchlistBottomSheet(details)
        }


    }

    private fun favoriteClick(details: TitleDetails) {
        binding.BTNFavorite.isEnabled = false

        val wasFavorite = details.isFavorite

        details.toggleFavorite()
        updateHeartUI(details.isFavorite)
        SignalManager.getInstance().vibrate()

        if (details.isFavorite) {
            FireStoreManager.getInstance()
                .addTitle(details.toMediaItem(), Constants.FIRESTORE.FAVORITES) { result ->
                    binding.BTNFavorite.isEnabled = true

                    if (result != Constants.logMessage.SUCCESS) {
                        details.isFavorite = wasFavorite
                        updateHeartUI(wasFavorite)
                        SignalManager.getInstance()
                            .toast("Connection error", SignalManager.ToastLength.SHORT)
                    } else {
                        SignalManager.getInstance()
                            .toast("Added to favorites", SignalManager.ToastLength.SHORT)
                    }
                }
        } else {
            FireStoreManager.getInstance()
                .removeTitle(details.id.toString(), Constants.FIRESTORE.FAVORITES) { success ->
                    binding.BTNFavorite.isEnabled = true

                    if (!success) {
                        details.isFavorite = wasFavorite
                        updateHeartUI(wasFavorite)
                        SignalManager.getInstance()
                            .toast("Connection error", SignalManager.ToastLength.SHORT)
                    } else {
                        SignalManager.getInstance()
                            .toast("Removed from favorites", SignalManager.ToastLength.SHORT)
                    }
                }
        }
    }


    private fun updateHeartUI(isFavorite: Boolean) {
        val icon = if (isFavorite) R.drawable.heart else R.drawable.empty_heart
        binding.BTNFavorite.setImageResource(icon)
    }

    private fun updateProviders(providers: List<ProviderItem>?) {
        if (providers.isNullOrEmpty()) {
            binding.rvProviders.visibility = View.GONE
            binding.NoProviders.visibility = View.VISIBLE
        } else {
            binding.rvProviders.visibility = View.VISIBLE
            binding.NoProviders.visibility = View.GONE
            providerAdapter.updateData(providers)
        }
    }

    private fun showWatchlistBottomSheet(details: TitleDetails) {
        val dialog = BottomSheetDialog(this, R.style.BottomSheetStyle)
        val sheetBinding = DialogWatchlistsBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)


        FireStoreManager.getInstance().showMyLists { watchLists ->
            val filteredList = watchLists.filter{it.id != Constants.FIRESTORE.FAVORITES }
            if (filteredList.isEmpty()) {
                sheetBinding.rvLists.visibility = View.GONE
                sheetBinding.LBLNoLists.visibility = View.VISIBLE
            } else {
                sheetBinding.rvLists.visibility = View.VISIBLE
                sheetBinding.LBLNoLists.visibility = View.GONE
                val callback = object : AddCallback {
                    override fun watchlistClicked(watchlist: Watchlist) {
                        FireStoreManager.getInstance()
                            .addTitle(details.toMediaItem(), watchlist.id) { result ->
                                sheetBinding.rvLists.isEnabled = true

                                when (result) {
                                    Constants.logMessage.SUCCESS -> {
                                        SignalManager.getInstance().toast(
                                            "Added to ${watchlist.listName}",
                                            SignalManager.ToastLength.SHORT
                                        )
                                        dialog.dismiss()
                                    }

                                    Constants.logMessage.EXISTS -> {
                                        SignalManager.getInstance().toast(
                                            "Already in  ${watchlist.listName}",
                                            SignalManager.ToastLength.LONG
                                        )
                                    }

                                    else -> {
                                        SignalManager.getInstance().toast(
                                            "Error, please try again",
                                            SignalManager.ToastLength.SHORT
                                        )
                                    }
                                }
                            }
                    }
                }
                val adapter = ListSelectAdapter(filteredList, details.id.toString(), callback)

                sheetBinding.rvLists.adapter = adapter
                sheetBinding.rvLists.layoutManager = LinearLayoutManager(this)
            }
        }
        dialog.show()
    }

    private fun TitleDetails.toMediaItem() = com.example.watchit_movieapp.model.MediaItem(
        id = this.id.toString(),
        title = if (this.mediaType == "movie") this.name else null,
        tvName = if (this.mediaType == "tv") this.name else null,
        relDate = if (this.mediaType == "movie") this.date else null,
        airDate = if (this.mediaType == "tv") this.date else null,
        poster = this.poster,
        rating = this.rating,
        overview = this.overview,
        mediaType = this.mediaType ?: "Unknown"
    )
}