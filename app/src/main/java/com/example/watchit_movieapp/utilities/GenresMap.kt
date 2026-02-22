package com.example.watchit_movieapp.utilities

object GenresMap {
    private val genres = mapOf(
        28 to "Action",
        12 to "Adventure",
        16 to "Animation",
        35 to "Comedy",
        80 to "Crime",
        99 to "Documentary",
        18 to "Drama",
        10751 to "Family",
        14 to "Fantasy",
        36 to "History",
        27 to "Horror",
        10402 to "Music",
        9648 to "Mystery",
        10749 to "Romance",
        878 to "Sci-Fi",
        10770 to "TV Movie",
        53 to "Thriller",
        10752 to "War",
        37 to "Western"
    )

    fun getGenreName(id: Int): String = genres[id] ?: "Other"

    fun getGenresString(ids: List<Int>?): String {
        if (ids.isNullOrEmpty()) return ""

        return ids.take(3).joinToString(", ") { getGenreName(it) }
    }
}