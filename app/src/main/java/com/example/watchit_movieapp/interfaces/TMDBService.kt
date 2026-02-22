package com.example.watchit_movieapp.interfaces


import com.example.watchit_movieapp.Model.MediaItem
import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query


interface TMDBService {
    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): TMDBResponse


    @GET("tv/popular")
    suspend fun getPopularTVShows(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): TMDBResponse
}

data class TMDBResponse(
    @SerializedName("results")
    val results: List<MediaItem>
)
