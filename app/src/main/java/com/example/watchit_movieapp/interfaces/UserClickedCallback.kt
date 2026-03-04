package com.example.watchit_movieapp.interfaces


import com.example.watchit_movieapp.model.User

interface UserClickedCallback {
    fun userClicked(user: User)
}