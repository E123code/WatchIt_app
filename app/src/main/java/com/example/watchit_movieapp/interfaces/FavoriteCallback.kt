package com.example.watchit_movieapp.interfaces

import com.example.watchit_movieapp.Model.MediaItem

interface FavoriteCallback {

    fun favoriteButtonClicked(movie: MediaItem, position: Int)
}