package com.example.watchit_movieapp.utilities

object Constants {

    object bundlekeys{
        const val ID_KEY: String ="ID"
        const val TYPE_KEY: String ="TYPE"
    }

    object logMessage{


        const val FIRESTORE_KEY = "FIRESTORE"
    }


    object FIRESTORE{
        const val Watchlist_REF = "WatchLists"
        const val USERS_REF = "UsersList"
        const val FAVORITES = "Favorites"
    }

    object TAGS{
        const val HOME_TAG = "HOME"
        const val SEARCH_TAG = "SEARCH"
        const val LISTS_TAG = "WATCHLISTS"
        const val PROFILE_TAG = "PROFILE"

    }
}