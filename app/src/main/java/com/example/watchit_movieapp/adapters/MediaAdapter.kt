package com.example.watchit_movieapp.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.watchit_movieapp.R
import com.example.watchit_movieapp.databinding.MediaItemBinding
import com.example.watchit_movieapp.interfaces.MediaItemClickedCallback
import com.example.watchit_movieapp.interfaces.TitleCallback
import com.example.watchit_movieapp.model.MediaItem
import com.example.watchit_movieapp.utilities.AdapterMode
import com.example.watchit_movieapp.utilities.FireStoreManager
import com.example.watchit_movieapp.utilities.GenresMap
import com.example.watchit_movieapp.utilities.ImageLoader

//adapter for media item cards
class MediaAdapter(
    private var items: List<MediaItem> = emptyList(),
    private val mode: AdapterMode,
    private val callback: MediaItemClickedCallback
) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    val currentItems: List<MediaItem>
        get() = items

    var titleCallback: TitleCallback? = null

    fun updateData(newMedia: List<MediaItem>) {
        newMedia.forEach { item ->
            item.isFavorite = FireStoreManager.getInstance().isInFavorites(item.id)
        }
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
        with(holder) {
            with(getItem(position)) {
                binding.Title.text = this.name
                binding.releaseYear.text = this.date
                binding.ageRating.text = this.ageRating
                binding.mediaType.isVisible = (mode != AdapterMode.HOME)
                binding.deleteBTN.isVisible = (mode == AdapterMode.MY_LIST)
                if (binding.mediaType.isVisible) {
                    binding.mediaType.text = if (this.mediaType == "movie") "Movie" else "TV Series"
                }

                binding.movieLBLGenres.text = GenresMap.getGenresString(this.genreIds)
                binding.info.text = this.overview
                binding.ratingBar.rating = (rating / 2).toFloat()
                ImageLoader.getInstance().loadImage(
                    fullPosterUrl,
                    binding.IMGPoster
                )
                binding.root.setOnClickListener {
                    callback.mediaItemClicked(this)
                }

                if (isFavorite) binding.IMGFavorite.setImageResource(R.drawable.heart)
                else binding.IMGFavorite.setImageResource(R.drawable.empty_heart)


            }
        }

    }


    fun getItem(position: Int): MediaItem = items[position]


    override fun getItemCount(): Int = items.size

    //removing item from list
    fun removeItem(position: Int) {
        if (position >= 0 && position < items.size) {
            val mutableList = items.toMutableList()
            mutableList.removeAt(position)
            items = mutableList

            notifyItemRemoved(position)
            notifyItemRangeChanged(position, items.size)
        }
    }

    //syncing the favorite status for the items on the list
    fun syncFavorites() {
        this.items.forEach { item ->
            item.isFavorite = FireStoreManager.getInstance().isInFavorites(item.id)
        }

        notifyDataSetChanged()
    }

    inner class MediaViewHolder(val binding: MediaItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.IMGFavorite.setOnClickListener {
                val position = absoluteAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    titleCallback?.favoriteButtonClicked(getItem(position), position)
                }
            }
            binding.deleteBTN.setOnClickListener {
                val position = absoluteAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    titleCallback?.deleteButtonClicked(getItem(position), position)
                }
            }

        }
    }
}
