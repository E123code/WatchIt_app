
package com.example.watchit_movieapp.model

import com.google.gson.annotations.SerializedName

//media item card data class
data class MediaItem(
    @SerializedName("id") val id: String = "",//the ID of it in TMDB API

    @SerializedName("poster_path")//the poster
    val poster: String? = null,

    @SerializedName("title")//name of the Title if movie
    val title: String? = null,

    @SerializedName("name")//name of the Title if TV show
    val tvName: String? = null,

    @SerializedName("release_date")// the date of release of movie
    val relDate: String? = null,

    @SerializedName("first_air_date")// the date of TV show aired
    val airDate: String? = null,

    @SerializedName("vote_average")//the rating
    val rating: Double =0.0,

    @SerializedName("overview")//overview
    val overview: String? = null,

    @SerializedName("genre_ids")//what genres it is classified as
    val genreIds: List<Int>? = emptyList(),

    @SerializedName("media_type")//movie or tv
    var mediaType: String? = null,

    @SerializedName("adult") val isAdult: Boolean = false,//if it is 18+ or not


    var isFavorite: Boolean = false//if it is in your favorite list or not
) {
    val name: String// to determine the name
        get() = title ?: tvName ?: "Unknown"

    val date: String// to determine  when the title came out
        get() = (relDate ?: airDate ?: "").take(4)

    val fullPosterUrl: String// to get poster path
        get() = "https://image.tmdb.org/t/p/w500${poster}"

    val ageRating: String// to determine age rating
        get() = if (isAdult) "18+" else "PG"

    fun toggleFavorite() = apply { isFavorite = !isFavorite}

}