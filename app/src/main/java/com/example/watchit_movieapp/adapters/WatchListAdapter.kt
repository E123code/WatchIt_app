package com.example.watchit_movieapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.watchit_movieapp.R
import com.example.watchit_movieapp.databinding.WatchlistItemBinding
import com.example.watchit_movieapp.interfaces.ListClickedCallback
import com.example.watchit_movieapp.model.Watchlist
import com.example.watchit_movieapp.utilities.AdapterMode
import com.example.watchit_movieapp.utilities.Constants

//adapter to show the watchLists of the user
class WatchlistAdapter(
    var lists: List<Watchlist> = emptyList(),
    private val mode: AdapterMode,
    private val callback: ListClickedCallback
) :RecyclerView.Adapter<WatchlistAdapter.WatchlistViewHolder>() {

    fun updateData(newLists: List<Watchlist>) {
        this.lists = newLists
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WatchlistViewHolder {
        val binding = WatchlistItemBinding
            .inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return WatchlistViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: WatchlistViewHolder,
        position: Int
    ) {
        with(holder) {
            with(getItem(position)) {
                binding.ListNameLBL.text = this.listName
                binding.ItemCountLBL.text = "${this.titleCount} titles"
                if (this.id == Constants.FIRESTORE.FAVORITES) {
                    binding.ListImage.setImageResource(R.drawable.heart)
                }else{
                    binding.ListImage.setImageResource(R.drawable.movie_list)
                }
                binding.BTNDeleteList.isVisible = (mode != AdapterMode.FRIEND_MODE && this.id != Constants.FIRESTORE.FAVORITES)
                binding.root.setOnClickListener {
                    callback.watchlistClicked(this)
                }
            }
        }
    }


    fun getItem(position: Int): Watchlist = lists[position]


    override fun getItemCount(): Int = lists.size

    inner class WatchlistViewHolder(val binding: WatchlistItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.BTNDeleteList.setOnClickListener {
                val position = absoluteAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    callback.deleteListClicked(getItem(position))

                }
            }
        }

    }
}