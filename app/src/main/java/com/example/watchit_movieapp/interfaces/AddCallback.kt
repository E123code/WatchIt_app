package com.example.watchit_movieapp.interfaces

import com.example.watchit_movieapp.model.Watchlist

//callback to  add item to list by clicking it on bottomSheet
interface AddCallback {
    fun watchlistClicked(watchlist: Watchlist)
}