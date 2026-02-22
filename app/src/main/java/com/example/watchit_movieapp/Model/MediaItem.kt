
package com.example.watchit_movieapp.Model

import com.google.gson.annotations.SerializedName

data class MediaItem(
    val id: Int = 0,

    @SerializedName("poster_path")
    val poster: String? = null,

    @SerializedName("title")
    val title: String? = null, // הורדנו private

    @SerializedName("name")
    val tvName: String? = null, // הורדנו private

    @SerializedName("release_date")
    val relDate: String? = null,

    @SerializedName("first_air_date")
    val airDate: String? = null,

    @SerializedName("vote_average")
    val rating: Float = 0f,

    @SerializedName("overview")
    val overview: String? = null,

    @SerializedName("genre_ids")
    val genreIds: List<Int>? = emptyList(),

    @SerializedName("media_type")
    val mediaType: String? = null,

    @SerializedName("adult") val isAdult: Boolean = false,


    var isFavorite: Boolean = false
) {
    val name: String
        get() = title ?: tvName ?: "Unknown"

    val date: String
        get() = (relDate ?: airDate ?: "").take(4)

    val fullPosterUrl: String
        get() = "https://image.tmdb.org/t/p/w500$poster"

    val ageRating: String
        get() = if (isAdult) "18+" else "PG"

    fun toggleFavorite() = apply { isFavorite = !isFavorite}

}