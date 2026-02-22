package com.example.watchit_movieapp.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.watchit_movieapp.R
import androidx.recyclerview.widget.RecyclerView
import com.example.watchit_movieapp.Model.MediaItem
import com.example.watchit_movieapp.databinding.MediaItemBinding
import com.example.watchit_movieapp.interfaces.FavoriteCallback
import com.example.watchit_movieapp.utilities.GenresMap
import com.example.watchit_movieapp.utilities.ImageLoader

class MediaAdapter(private var items: List<MediaItem> = emptyList(),
                   private val isSearchMode: Boolean = false) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>(){

    var favoriteCallback: FavoriteCallback? = null

    fun updateData(newMedia: List<MediaItem>) {
        this.items = newMedia
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MediaViewHolder {
        val binding = MediaItemBinding
            .inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return MediaViewHolder(binding)
    }

        override fun onBindViewHolder(
            holder: MediaViewHolder,
            position: Int
        ) {
            with(holder){
                with(getItem(position)){
                    Log.d("ADAPTER", "Binding movie at position: $position")
                    binding.Title.text = this.name
                    binding.releaseYear.text= this.date
                    binding.ageRating.text = this.ageRating
                    if (isSearchMode) {
                        binding.mediaType.visibility = View.VISIBLE
                        binding.mediaType.text = if (this.mediaType == "movie") "Movie" else "TV Series"
                    } else {
                        binding.mediaType.visibility = View.GONE
                    }
                    binding.movieLBLGenres.text = GenresMap.getGenresString(this.genreIds)
                    binding.info.text= this.overview
                    binding.ratingBar.rating = rating / 2
                    ImageLoader.getInstance().loadImage(
                        fullPosterUrl,
                        binding.IMGPoster
                    )
                    if (isFavorite) binding.IMGFavorite.setImageResource(R.drawable.heart)
                    else binding.IMGFavorite.setImageResource(R.drawable.empty_heart)


                }
            }


        }


        fun getItem(position: Int): MediaItem = items[position]// getting the high score by index


        override fun getItemCount(): Int = items.size //amount of records


    inner class MediaViewHolder(val binding: MediaItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.IMGFavorite.setOnClickListener {
                val position = absoluteAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    favoriteCallback?.favoriteButtonClicked(getItem(position), position)
                }
            }
        }
    }
}
