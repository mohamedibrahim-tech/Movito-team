package com.movito.movito.data.source.remote

import com.movito.movito.data.model.Genre
import com.movito.movito.data.model.Movie
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApi {

    @GET("genre/movie/list")
    suspend fun getGenres(@Query("api_key") apiKey: String): GenreResponse

    @GET("discover/movie")
    suspend fun discoverMoviesByGenre(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int,
        @Query("with_genres") genreId: Int,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("primary_release_year") primaryReleaseYear: Int
    ): MovieResponse

    @GET("movie/{movie_id}/videos")
    suspend fun getMovieVideos(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String
    ): VideoResponse

}

data class MovieResponse(
    val results: List<Movie>
)

data class GenreResponse(
    val genres: List<Genre>
)

data class VideoResponse(
    val results: List<Video>
)

data class Video(
    val key: String,
    val site: String,
    val type: String,
    val official: Boolean
)
