package com.example.watchit_movieapp.interfaces


import com.example.watchit_movieapp.model.Watchlist

interface ListClickedCallback {
    fun watchlistClicked(watchlist: Watchlist)
    fun deleteListClicked(watchlist: Watchlist)
}