package com.example.watchit_movieapp.utilities

import com.example.watchit_movieapp.BuildConfig
import com.example.watchit_movieapp.interfaces.TMDBResponse
import com.example.watchit_movieapp.interfaces.TMDBService
import com.example.watchit_movieapp.model.TitleDetails
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://api.themoviedb.org/3/"
    const val API_KEY = BuildConfig.TMDB_API_KEY

    val instance: TMDBService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(TMDBService::class.java)
    }

    suspend fun getPopularMovies(page: Int = 1): TMDBResponse {
       return instance.getPopularMovies(API_KEY)
    }


    suspend fun getPopularTVShows(page: Int = 1): TMDBResponse {
        return instance.getPopularTVShows(API_KEY)
    }


    suspend fun getDetails(id: Int, type: String) : TitleDetails{
        return  instance.getTitleDetails(type,id,API_KEY)
    }
}