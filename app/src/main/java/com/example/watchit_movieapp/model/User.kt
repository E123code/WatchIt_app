package com.example.watchit_movieapp.model

data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val profileImageUrl: String = "",
    val favorites: ArrayList<String> = arrayListOf(),//ids of the favorites for easy access
    val friendsList: List<String> = emptyList()//the ids of users friends
)
