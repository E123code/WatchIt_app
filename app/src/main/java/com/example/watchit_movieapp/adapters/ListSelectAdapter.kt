package com.example.watchit_movieapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.watchit_movieapp.R
import com.example.watchit_movieapp.databinding.ListSelectItemBinding
import com.example.watchit_movieapp.interfaces.AddCallback
import com.example.watchit_movieapp.model.Watchlist

class ListSelectAdapter(
    private val lists: List<Watchlist>,
    private val currentTitleId: String,
    private val callback: AddCallback
) : RecyclerView.Adapter<ListSelectAdapter.ListViewHolder>() {

    private var lastClickTime: Long = 0




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding = ListSelectItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        with(holder) {
            with(getItem(position)) {

                binding.LBLListName.text = this.listName
                binding.LBLItemCount.text = "${this.titleCount} titles"

                val isAlreadyInList = this.items.contains(currentTitleId)

                if (isAlreadyInList) {
                    binding.IMGStatus.setImageResource(R.drawable.checked_icon)
                    holder.itemView.alpha = 0.5f
                } else {
                   binding.IMGStatus.setImageResource(R.drawable.add_btn)
                    holder.itemView.alpha = 1.0f
                }

                itemView.setOnClickListener {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastClickTime > 500) {
                        lastClickTime = currentTime
                        if(!isAlreadyInList) {
                            callback.watchlistClicked(this)
                        }
                    }
                }
            }
        }
    }

            override fun getItemCount() = lists.size


            inner class ListViewHolder(val binding: ListSelectItemBinding) :
                RecyclerView.ViewHolder(binding.root)

            fun getItem(position: Int): Watchlist = lists[position]
        }
