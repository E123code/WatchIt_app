package com.example.watchit_movieapp

import android.os.Bundle
import android.util.Log
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
import com.example.watchit_movieapp.model.User
import com.example.watchit_movieapp.utilities.AdapterMode
import com.example.watchit_movieapp.utilities.Constants
import com.example.watchit_movieapp.utilities.FireStoreManager
import com.example.watchit_movieapp.utilities.SignalManager
import com.example.watchit_movieapp.utilities.openDetails
import com.google.firebase.auth.FirebaseAuth

class WatchlistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWatchlistBinding
    private lateinit var mediaAdapter: MediaAdapter

    private  var myId : String = ""
    private var listOwner: String = ""
    private var listID: String = ""
    private var listName: String = ""

     private val userObserver :(User?)-> Unit={ _->
        refreshUI()
     }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWatchlistBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        myId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        initViews()
        FireStoreManager.getInstance().addObserver(userObserver)
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
                val viewHolder =
                    binding.RVListTitles.findViewHolderForAdapterPosition(position) as? MediaAdapter.MediaViewHolder
                viewHolder?.binding?.IMGFavorite?.isEnabled = false

                val previous = title.isFavorite
                title.toggleFavorite()
                mediaAdapter.notifyItemChanged(position)
                SignalManager.getInstance().vibrate()

                if (title.isFavorite) {
                    FireStoreManager.getInstance()
                        .addTitle(title, Constants.FIRESTORE.FAVORITES) { result ->
                            viewHolder?.binding?.IMGFavorite?.isEnabled = true
                            if (result != Constants.logMessage.SUCCESS) {
                                title.isFavorite = previous
                                mediaAdapter.notifyItemChanged(position)
                                SignalManager.getInstance()
                                    .toast("Error", SignalManager.ToastLength.SHORT)
                            } else {
                                SignalManager.getInstance()
                                    .toast("Added to favorites", SignalManager.ToastLength.SHORT)
                            }
                        }
                } else {
                    FireStoreManager.getInstance()
                        .removeTitle(title.id, Constants.FIRESTORE.FAVORITES) { success ->
                            if (!success) {
                                title.isFavorite = previous
                                viewHolder?.binding?.IMGFavorite?.isEnabled = true
                                SignalManager.getInstance()
                                    .toast("Error", SignalManager.ToastLength.SHORT)
                            } else {
                                if (listID != Constants.FIRESTORE.FAVORITES || listOwner != myId){
                                    viewHolder?.binding?.IMGFavorite?.isEnabled = true
                                    mediaAdapter.notifyItemChanged(position)
                                }
                                SignalManager.getInstance()
                                    .toast(
                                        "Removed from favorites",
                                        SignalManager.ToastLength.SHORT
                                    )
                            }
                        }
                }
            }


            override fun deleteButtonClicked(title: MediaItem, position: Int) {
                FireStoreManager.getInstance().removeTitle(title.id, listID) { success ->
                    if (success) {
                        removeLocally(position)
                        SignalManager.getInstance()
                            .toast("deleted from Watchlist", SignalManager.ToastLength.SHORT)
                    }else{
                        SignalManager.getInstance()
                            .toast("error", SignalManager.ToastLength.SHORT)
                    }
                }
            }
        }

        binding.RVListTitles.adapter = mediaAdapter
        binding.RVListTitles.layoutManager = LinearLayoutManager(this)

    }

//removes locally form the adapter list
    private fun removeLocally(position: Int) {
        mediaAdapter.removeItem(position)

        if (mediaAdapter.itemCount == 0) {
            binding.emptyListLBL.visibility = View.VISIBLE
        }
    }

    private fun loadTitles() {
        FireStoreManager.getInstance().loadWatchlist(listOwner, listID) { titles ->
            showList(titles)
        }
    }

    private fun showList(titles: List<MediaItem>) {
        if (titles.isEmpty()) {
            binding.RVListTitles.visibility = View.GONE
            binding.emptyListLBL.visibility = View.VISIBLE
        } else {
            binding.RVListTitles.visibility = View.VISIBLE
            binding.emptyListLBL.visibility = View.GONE
            mediaAdapter.updateData(titles)
        }
    }

    private fun refreshUI() {
        if (::mediaAdapter.isInitialized) {

            if (listID == Constants.FIRESTORE.FAVORITES && listOwner == myId) {
                val updatedList =
                    mediaAdapter.currentItems.filter { item ->
                        FireStoreManager.getInstance().isInFavorites(item.id)
                    }

                Log.d(
                    "CHECK",
                    "Items count before: ${mediaAdapter.itemCount}, after filter: ${updatedList.size}"
                )
                if (updatedList.size != mediaAdapter.itemCount) {
                    mediaAdapter.updateData(updatedList)
                    binding.emptyListLBL.visibility =
                        if (updatedList.isEmpty()) View.VISIBLE else View.GONE
                }
            } else {
                mediaAdapter.syncFavorites()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshUI()
    }


    override fun onDestroy() {
        super.onDestroy()
        FireStoreManager.getInstance().removeObserver(userObserver)
    }

}