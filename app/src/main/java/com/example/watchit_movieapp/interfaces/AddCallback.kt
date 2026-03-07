package com.example.watchit_movieapp.interfaces

import com.example.watchit_movieapp.model.Watchlist

interface AddCallback {
    fun watchlistClicked(watchlist: Watchlist)
}