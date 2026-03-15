package com.example.watchit_movieapp.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.watchit_movieapp.R
import com.example.watchit_movieapp.WatchlistActivity
import com.example.watchit_movieapp.adapters.WatchlistAdapter
import com.example.watchit_movieapp.databinding.ListsFragmentBinding
import com.example.watchit_movieapp.interfaces.ListClickedCallback
import com.example.watchit_movieapp.model.Watchlist
import com.example.watchit_movieapp.utilities.AdapterMode
import com.example.watchit_movieapp.utilities.Constants
import com.example.watchit_movieapp.utilities.FireStoreManager
import com.example.watchit_movieapp.utilities.SignalManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration

class ListsFragment : Fragment() {

    private var _binding: ListsFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var watchlistAdapter: WatchlistAdapter

    private var listsListener: ListenerRegistration? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ListsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        binding.BTNAddList.setOnClickListener {
            addList()
        }


    }


    /**
     * sets up the Watchlist recycleView
     */
    private fun setupRecyclerView() {
        val callback = object : ListClickedCallback {
            override fun watchlistClicked(watchlist: Watchlist) {
                val intent = Intent(requireContext(), WatchlistActivity::class.java)
                var bundle = Bundle()
                bundle.putString(
                    Constants.bundlekeys.ID_KEY,
                    FirebaseAuth.getInstance().currentUser?.uid
                )
                if (watchlist.id == Constants.FIRESTORE.FAVORITES) {
                    bundle.putString(
                        Constants.bundlekeys.LIST_ID_KEY,
                        Constants.FIRESTORE.FAVORITES
                    )
                    bundle.putString(Constants.bundlekeys.LIST_NAME_KEY, "Favorites")
                } else {
                    bundle.putString(Constants.bundlekeys.LIST_ID_KEY, watchlist.id)
                    bundle.putString(Constants.bundlekeys.LIST_NAME_KEY, watchlist.listName)
                }
                intent.putExtras(bundle)
                startActivity(intent)
            }

            override fun deleteListClicked(watchlist: Watchlist) {
                showDeleteConfirmation(watchlist)

            }
        }

        watchlistAdapter =
            WatchlistAdapter(emptyList(), mode = AdapterMode.NATURAL, callback = callback)

        binding.RVLists.adapter = watchlistAdapter
        binding.RVLists.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun showDeleteConfirmation(watchlist: Watchlist) {
        MaterialAlertDialogBuilder(requireContext(), R.style.CustomAlertDialog)
            .setTitle("delete list")
            .setMessage("Are you sure you want to delete '${watchlist.listName}'?")
            .setPositiveButton("delete") { _, _ ->
                FireStoreManager.getInstance().deleteWatchlist(watchlist.id) { success ->
                    if (success) {
                        SignalManager.getInstance()
                            .toast("The list Deleted", SignalManager.ToastLength.SHORT)
                    } else {
                        SignalManager.getInstance().toast("Error", SignalManager.ToastLength.SHORT)
                    }
                }
            }
            .setNegativeButton("cancel", null)
            .show()
    }

    //adding new watchList
    /**
     * opens a dialog to enter the name of the new list
     */
    private fun addList() {
        val input = EditText(requireContext())
        input.hint = "Enter list name"
        input.setTextColor(Color.WHITE)
        input.setHintTextColor(Color.GRAY)

        val container = FrameLayout(requireContext())
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(60, 20, 60, 0)
        input.layoutParams = params
        container.addView(input)

        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.CustomAlertDialog)
            .setMessage("Enter watchlist name")
            .setView(container)
            .setPositiveButton("Create") { _, _ ->
                val listName = input.text.toString().trim()
                if (listName.isNotEmpty()) {
                    createNewList(listName)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()

    }

    /**
     * gets the new list name from the dialog and creates new watchList for the user
     */
    private fun createNewList(listName: String) {
        FireStoreManager.getInstance().addWatchList(listName) { success ->
            if (success) {
                SignalManager.getInstance()
                    .toast("Watchlist created!", SignalManager.ToastLength.SHORT)
            } else {
                SignalManager.getInstance()
                    .toast("Error creating list", SignalManager.ToastLength.SHORT)
            }
        }
    }

    //start the snapshot listener
    private fun startListsListen() {
        if (listsListener == null) {
            listsListener = FireStoreManager.getInstance().showMyLists { watchlists ->

                watchlistAdapter.updateData(watchlists)
            }
        }
    }

    //stop the snapshot listener
    private fun stopListListen() {

        listsListener?.remove()
        listsListener = null
    }



    override fun onResume() {
        super.onResume()
        if(!isHidden){
            startListsListen()

        }
    }

    override fun onPause() {
        super.onPause()
        stopListListen()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) stopListListen()
        else {
            startListsListen()
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        listsListener?.remove()
        _binding = null
    }

}