package com.example.watchit_movieapp.interfaces


import com.example.watchit_movieapp.model.Watchlist

//callback for  list in lists fragment
interface ListClickedCallback {
    fun watchlistClicked(watchlist: Watchlist)//what happens when you click one of the lists
    fun deleteListClicked(watchlist: Watchlist)//what happens when you delete one of the lists
}