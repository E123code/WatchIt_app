package com.example.watchit_movieapp.model

data class Watchlist (
    val id : String = "",
    val listName: String = "",
    var titleCount:Int = 0,//number of items in list
    val items: List<String> = emptyList()//Ids of media items in watch list
    )