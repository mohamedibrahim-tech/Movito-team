package com.movito.movito.data.source.remote

import com.movito.movito.data.model.Movie
import retrofit2.http.GET
import retrofit2.http.Query

interface TmdbApi {

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int
    ): MovieResponse

}

data class MovieResponse(
    val results: List<Movie>
)
