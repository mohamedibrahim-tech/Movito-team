package com.movito.movito.TMDPAPI

import com.movito.movito.data.source.remote.MovieResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface MovieCallable {

    @GET("discover/movie")
    fun discoverMovies(
        @Query("api_key") apiKey: String,
        @Query("region") region: String
    ): Call<MovieResponse>

}