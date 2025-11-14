package com.movito.movito.data.source.remote

import com.movito.movito.data.model.Movie
import com.movito.movito.data.model.Genre
import retrofit2.http.GET
import retrofit2.http.Query

interface TmdbApi {

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int
    ): MovieResponse

    @GET("genre/movie/list")
    suspend fun getGenres(@Query("api_key") apiKey: String): GenreResponse

    @GET("discover/movie")
    suspend fun discoverMoviesByGenre(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int,
        @Query("with_genres") genreId: Int
    ): MovieResponse

}

data class MovieResponse(
    val results: List<Movie>
)

data class GenreResponse(
    val genres: List<Genre>
)
