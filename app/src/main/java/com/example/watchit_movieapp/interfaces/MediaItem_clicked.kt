package com.example.watchit_movieapp.interfaces

import com.example.watchit_movieapp.model.MediaItem

//callback for clicking one od the media Item cards
interface MediaItemClickedCallback {

    fun mediaItemClicked(mediaItem: MediaItem)
}