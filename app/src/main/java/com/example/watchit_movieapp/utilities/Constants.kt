package com.example.watchit_movieapp.utilities

object Constants {

    object bundlekeys{
        const val ID_KEY: String ="ID"
        const val TYPE_KEY: String ="TYPE"
        const val LIST_ID_KEY  = "LIST_ID"
        const val  LIST_NAME_KEY ="LIST_NAME"

    }

    object logMessage{

        const val HOME_KEY = "HOME"
        const val DETAILS_KEY = "DETAILS"
        const val FIRESTORE_KEY = "FIRESTORE"
    }


    object FIRESTORE{
        const val WATCHLISTS_REF = "WatchLists"
        const val USERS_REF = "UsersList"
        const val FAVORITES = "Favorites"
        const val TITLES_REF = "Titles"

        const val RATINGS = "Ratings"
    }

    object TAGS{
        const val HOME_TAG = "HOME"
        const val SEARCH_TAG = "SEARCH"
        const val LISTS_TAG = "WATCHLISTS"
        const val PROFILE_TAG = "PROFILE"

    }
}