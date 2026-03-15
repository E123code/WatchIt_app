package com.example.watchit_movieapp.utilities

enum class AdapterMode {
    HOME,//doesn't show delete button and media type
    MY_LIST,// shows media type and delete button
    FRIEND_MODE,//doesn't show delete button for watchLists
    NATURAL//shows media type and not delete button
}