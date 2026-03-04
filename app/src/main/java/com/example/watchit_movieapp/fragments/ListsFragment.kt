package com.example.watchit_movieapp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.watchit_movieapp.WatchlistActivity
import com.example.watchit_movieapp.adapters.WatchlistAdapter
import com.example.watchit_movieapp.databinding.ListsFragmentBinding
import com.example.watchit_movieapp.interfaces.ListClickedCallback
import com.example.watchit_movieapp.model.Watchlist
import com.example.watchit_movieapp.utilities.FireStoreManager
import com.google.firebase.firestore.ListenerRegistration

class ListsFragment : Fragment() {

    private var _binding: ListsFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var watchlistAdapter: WatchlistAdapter

    private var listsListener: ListenerRegistration? = null
    private var favListener: ListenerRegistration? = null

    private var favCount: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ListsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        favListener = FireStoreManager.loadCurrentUser() { user ->
            favCount = user.favorites.size

            if (::watchlistAdapter.isInitialized && watchlistAdapter.lists.isNotEmpty()) {
                watchlistAdapter.lists[0].titleCount = favCount
                watchlistAdapter.notifyItemChanged(0)
            }
        }

        listsListener = FireStoreManager.showMyLists { watchlists ->
            watchlistAdapter.updateData(watchlists, favCount)
        }

        binding.BTNAddList.setOnClickListener {

        }

    }

    private fun setupRecyclerView() {
        val callback = object : ListClickedCallback {
            override fun watchlistClicked(watchlist: Watchlist) {
                val intent = Intent(requireContext(), WatchlistActivity::class.java)
                var bundle = Bundle()
                if (watchlist.id == "FAVORITES_ID") {
                    bundle.putString("LIST_ID", "FAVORITES_SPECIAL_ID")
                    bundle.putString("LIST_NAME", "My Favorites")
                } else {
                    bundle.putString("LIST_ID", watchlist.id)
                    bundle.putString("LIST_NAME", watchlist.listName)
                }
                intent.putExtras(bundle)
                startActivity(intent)
            }

            override fun deleteListClicked(watchlist: Watchlist, position: Int) {


            }
        }


        val initialList = listOf(Watchlist(id = "FAVORITES_ID", "My Favorites", favCount))

        watchlistAdapter = WatchlistAdapter(initialList, callback = callback)
    }


    override fun onResume() {
        super.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

}