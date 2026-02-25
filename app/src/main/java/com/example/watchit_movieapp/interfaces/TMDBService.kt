package com.example.watchit_movieapp.interfaces


import com.example.watchit_movieapp.model.TitleDetails
import com.example.watchit_movieapp.model.MediaItem
import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


interface TMDBService {
    @GET("trending/movie/day")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): TMDBResponse


    @GET("trending/tv/day")
    suspend fun getPopularTVShows(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): TMDBResponse

    @GET("{type}/{id}")
    suspend fun getTitleDetails(
        @Path("type") type: String, // "movie" או "tv"
        @Path("id") id: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("append_to_response") append: String = "credits,watch/providers,release_dates,content_ratings"
    ): TitleDetails
}

data class TMDBResponse(
    @SerializedName("results")
    val results: List<MediaItem>
)
