package com.example.watchit_movieapp.interfaces

import com.example.watchit_movieapp.model.MediaItem

//callback for titles
interface TitleCallback {

    fun favoriteButtonClicked(title: MediaItem, position: Int)// what happens when you press heart icon
    fun deleteButtonClicked(title: MediaItem, position: Int)// what happens when you delete one form watchList
}