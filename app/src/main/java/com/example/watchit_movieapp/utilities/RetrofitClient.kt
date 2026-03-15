package com.example.watchit_movieapp.utilities

import com.example.watchit_movieapp.BuildConfig
import com.example.watchit_movieapp.interfaces.TMDBResponse
import com.example.watchit_movieapp.interfaces.TMDBService
import com.example.watchit_movieapp.model.TitleDetails
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//client to get the responses from retrofit TMDB service
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
        val randomPage = (1..50).random()//gets random page of results
       return instance.getPopularMovies(API_KEY,page=randomPage)
    }


    suspend fun getPopularTVShows(page: Int = 1): TMDBResponse {
        val randomPage = (1..50).random()//gets random page of results
        return instance.getPopularTVShows(API_KEY,page=randomPage)
    }

    //gets the title details based on ID and media type
    suspend fun getDetails(id: Int, type: String) : TitleDetails{
        return  instance.getTitleDetails(type,id,API_KEY)
    }

    //searches all the results of titles with the same name in the query (e.g. batman,stranger things)
    suspend fun searchByName(query: String) : TMDBResponse{
        return instance.SearchByName(API_KEY,query)
    }
}