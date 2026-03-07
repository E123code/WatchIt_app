package com.example.watchit_movieapp

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.watchit_movieapp.adapters.MediaAdapter
import com.example.watchit_movieapp.databinding.ActivityWatchlistBinding
import com.example.watchit_movieapp.interfaces.MediaItemClickedCallback
import com.example.watchit_movieapp.interfaces.TitleCallback
import com.example.watchit_movieapp.model.MediaItem
import com.example.watchit_movieapp.utilities.AdapterMode
import com.example.watchit_movieapp.utilities.Constants
import com.example.watchit_movieapp.utilities.FireStoreManager
import com.example.watchit_movieapp.utilities.SignalManager
import com.example.watchit_movieapp.utilities.openDetails
import com.google.firebase.auth.FirebaseAuth

class WatchlistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWatchlistBinding
    private lateinit var mediaAdapter: MediaAdapter

    private var isFavorites = false

    private var listOwner: String = ""
    private var listID: String = ""
    private var listName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWatchlistBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
    }

    private fun initViews() {
        val bundle: Bundle? = intent.extras
        listOwner = bundle?.getString(Constants.bundlekeys.ID_KEY) ?: ""
        listID = bundle?.getString(Constants.bundlekeys.LIST_ID_KEY) ?: ""
        listName = bundle?.getString(Constants.bundlekeys.LIST_NAME_KEY) ?: ""

        binding.watchlistTitle.text = listName

        binding.BTNBack.setOnClickListener { finish() }

        setupRecycleView()
        loadTitles()

    }

    private fun setupRecycleView() {
        val myId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val mode = when {
            myId != listOwner -> AdapterMode.FRIEND_MODE
            listID == Constants.FIRESTORE.FAVORITES -> AdapterMode.NATURAL
            else -> AdapterMode.MY_LIST
        }

        val callback = object : MediaItemClickedCallback {
            override fun mediaItemClicked(mediaItem: MediaItem) {
                openDetails(mediaItem)
            }
        }

        mediaAdapter = MediaAdapter(emptyList(), mode, callback)

        mediaAdapter.titleCallback = object : TitleCallback {
            override fun favoriteButtonClicked(title: MediaItem, position: Int) {
                val previous = title.isFavorite
                title.toggleFavorite()
                SignalManager.getInstance().vibrate()
                mediaAdapter.notifyItemChanged(position)

                if (title.isFavorite) {
                    FireStoreManager.addFavorite(title) { success ->
                        if (!success) {
                            abortChange(title, previous, position)
                        } else {
                            SignalManager.getInstance()
                                .toast("added to favorites", SignalManager.ToastLength.SHORT)
                        }

                    }
                } else {
                    FireStoreManager.deleteFavorite(title.id) { success ->
                        if (success) {
                            if (listID == Constants.FIRESTORE.FAVORITES) {
                                removeLocally(position)
                            }
                            SignalManager.getInstance()
                                .toast("deleted from favorites", SignalManager.ToastLength.SHORT)
                        } else {
                            abortChange(title, previous, position)
                        }
                    }
                }
            }

            override fun deleteButtonClicked(title: MediaItem, position: Int) {
                FireStoreManager.removeTitle(title.id, listID) { success ->
                    if (success) {
                        removeLocally(position)
                        SignalManager.getInstance()
                            .toast("deleted from Watchlist", SignalManager.ToastLength.SHORT)
                    }
                }
            }
        }

        binding.RVListTitles.adapter = mediaAdapter
        binding.RVListTitles.layoutManager = LinearLayoutManager(this)

    }

    private fun abortChange(item: MediaItem, previousState: Boolean, position: Int) {
        item.isFavorite = previousState
        mediaAdapter.notifyItemChanged(position)
        SignalManager.getInstance().toast("Connection error", SignalManager.ToastLength.SHORT)
    }

    private fun removeLocally(position: Int) {
        mediaAdapter.removeItem(position)

        if (mediaAdapter.itemCount == 0) {
            binding.emptyListLBL.visibility = View.VISIBLE
        }
    }

    private fun loadTitles(){
        if(listID== Constants.FIRESTORE.FAVORITES){
            FireStoreManager.showFavorites(listOwner){titles ->
                showList(titles)
            }
        }else{
            FireStoreManager.loadWatchlist(listOwner,listID){titles ->
                showList(titles)
            }
        }

    }

    private fun showList(titles: List<MediaItem>){
        if(titles.isEmpty()){
            binding.RVListTitles.visibility = View.GONE
            binding.emptyListLBL.visibility = View.VISIBLE
        }else{
            binding.RVListTitles.visibility = View.VISIBLE
            binding.emptyListLBL.visibility = View.GONE
            mediaAdapter.updateData(titles)
        }
    }


}