package com.example.watchit_movieapp.interfaces

import com.example.watchit_movieapp.model.MediaItem

interface TitleCallback {

    fun favoriteButtonClicked(title: MediaItem, position: Int)
    fun deleteButtonClicked(item: MediaItem, position: Int)
}