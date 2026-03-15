package com.example.watchit_movieapp.interfaces


import com.example.watchit_movieapp.model.TitleDetails
import com.example.watchit_movieapp.model.MediaItem
import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

//the TMDB API Queries
interface TMDBService {
    @GET("movie/popular")//query to a page of popular movies (20 results)
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): TMDBResponse


    @GET("tv/popular")//query to a page of popular TV shows (20 results)
    suspend fun getPopularTVShows(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): TMDBResponse

    @GET("{type}/{id}")//query to get the title details by its type and ID
    suspend fun getTitleDetails(
        @Path("type") type: String, // "movie" או "tv"
        @Path("id") id: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("append_to_response") append: String = "credits,watch/providers,release_dates,content_ratings"
    ): TitleDetails

    @GET("search/multi")//query to search the title by its name
    suspend fun  SearchByName(
        @Query("api_key") apiKey: String,
        @Query("query") query: String
    ): TMDBResponse


}
//data class to contain response from TMDB API
data class TMDBResponse(
    @SerializedName("results")
    val results: List<MediaItem>
)
