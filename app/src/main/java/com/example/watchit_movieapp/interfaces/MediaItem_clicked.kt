package com.example.watchit_movieapp.interfaces

import com.example.watchit_movieapp.model.MediaItem

interface MediaItemClickedCallback {

    fun mediaItemClicked(mediaItem: MediaItem)
}