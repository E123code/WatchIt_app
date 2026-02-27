package com.example.watchit_movieapp.model

data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val profileImageUrl: String = "",
    val favorites: List<Int> = emptyList(), // רשימת ה-IDs של האהובים
    val friendsList: List<String> = emptyList()
)
