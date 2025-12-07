package com.movito.movito.data.source.remote

import com.movito.movito.data.model.Genre
import com.movito.movito.data.model.Movie
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import okio.IOException
import retrofit2.HttpException

/**
 * Retrofit interface defining all TMDB API endpoints used by the Movito app.
 *
 * This interface declares suspend functions for asynchronous API calls using Retrofit.
 * All methods are coroutine-friendly and return deserialized response objects.
 *
 * Key API categories:
 * - Movie discovery and filtering
 * - Genre information
 * - Movie search
 * - Video content (trailers)
 * - Movie recommendations
 *
 * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech/)
 *
 * @see MovieResponse for movie list responses
 * @see GenreResponse for genre list responses
 * @see VideoResponse for video list responses
 *
 * @since 13 Nov 2025
 * @see <a href="https://developers.themoviedb.org/3">TMDB API Documentation</a>
 */
interface TmdbApi {

    /**
     * Fetches the list of available movie genres from TMDB.
     *
     * This endpoint returns all movie genres (e.g., Action, Comedy, Drama) available in TMDB.
     * The response includes both genre IDs and localized names.
     *
     * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech/)
     *
     * @param apiKey TMDB API key for authentication
     * @param language ISO 639-1 language code (default: "en-US")
     * @return [GenreResponse] containing a list of [Genre] objects
     *
     * @throws HttpException for HTTP error responses (4xx, 5xx)
     * @throws IOException for network-related errors
     *
     * @since 14 Nov 2025
     */
    @GET("genre/movie/list")
    suspend fun getGenres(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
    ): GenreResponse

    /**
     * Discovers movies filtered by a specific genre.
     *
     * This endpoint returns a paginated list of movies belonging to the specified genre.
     * Results are sorted by popularity by default (TMDB default ordering).
     *
     * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech/)
     *
     * @param apiKey TMDB API key for authentication
     * @param page Page number for pagination (1-indexed)
     * @param genreId TMDB genre ID to filter by
     * @param language ISO 639-1 language code (default: "en-US")
     * @return [MovieResponse] containing a paginated list of [Movie] objects
     *
     * @throws HttpException for HTTP error responses (4xx, 5xx)
     * @throws IOException for network-related errors
     *
     * @since 14 Nov 2025
     */
    @GET("discover/movie")
    suspend fun discoverMoviesByGenre(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int,
        @Query("with_genres") genreId: Int,
        @Query("language") language: String = "en-US",
    ): MovieResponse

    /**
     * Searches for movies by title or keyword.
     *
     * This endpoint performs a full-text search across movie titles and overviews.
     * Results are sorted by relevance by default.
     *
     * **Author**: Movito Development Team Member [Yahia Mohamed](https://github.com/YahiaMohamed24)
     *
     * @param apiKey TMDB API key for authentication
     * @param query Search query string
     * @param language ISO 639-1 language code (default: "en-US")
     * @return [MovieResponse] containing search results as a list of [Movie] objects
     *
     * @throws HttpException for HTTP error responses (4xx, 5xx)
     * @throws IOException for network-related errors
     *
     * @since 22 Nov 2025
     */
    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("language") language: String = "en-US",
    ): MovieResponse

    /**
     * Fetches video content (trailers, teasers, clips) for a specific movie.
     *
     * This endpoint returns all available videos for a movie, including trailers, teasers,
     * clips, and behind-the-scenes content. Videos are filtered by site (YouTube, Vimeo, etc.)
     * and type (Trailer, Teaser, Clip, etc.).
     *
     * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech/)
     *
     * @param movieId TMDB movie ID
     * @param apiKey TMDB API key for authentication
     * @param language ISO 639-1 language code (default: "en-US")
     * @return [VideoResponse] containing a list of [Video] objects
     *
     * @throws HttpException for HTTP error responses (4xx, 5xx)
     * @throws IOException for network-related errors
     *
     * @since 14 Nov 2025
     */
    @GET("movie/{movie_id}/videos")
    suspend fun getMovieVideos(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
    ): VideoResponse

    /**
     * Fetches movie recommendations based on a specific movie.
     *
     * This endpoint returns movies that are similar or recommended based on the
     * specified movie. Recommendations are generated by TMDB's algorithm based
     * on viewing patterns, genres, keywords, and other metadata.
     *
     * **Author**: Movito Development Team Member [Ahmed Essam](https://github.com/ahmed-essam-dev/)
     *
     * @param movieId TMDB movie ID to get recommendations for
     * @param apiKey TMDB API key for authentication
     * @param language ISO 639-1 language code (default: "en-US")
     * @return [MovieResponse] containing a list of recommended [Movie] objects
     *
     * @throws HttpException for HTTP error responses (4xx, 5xx)
     * @throws IOException for network-related errors
     *
     * @since 24 Nov 2025
     */
    @GET("movie/{movie_id}/recommendations")
    suspend fun getMovieRecommendations(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
    ): MovieResponse

}

/**
 * Data class representing a paginated response of movies from TMDB API.
 *
 * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech/)
 *
 * @property results List of [Movie] objects in the current page
 *
 * @see Movie for individual movie details
 *
 * @since 13 Nov 2025
 */
data class MovieResponse(
    val results: List<Movie>
)

/**
 * Data class representing a response containing movie genres from TMDB API.
 *
 * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech/)
 *
 * @property genres List of [Genre] objects
 *
 * @see Genre for individual genre details
 *
 * @since 14 Nov 2025
 */
data class GenreResponse(
    val genres: List<Genre>
)

/**
 * Data class representing a response containing video content from TMDB API.
 *
 * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech/)
 *
 * @property results List of [Video] objects
 *
 * @see Video for individual video details
 *
 * @since 14 Nov 2025
 */
data class VideoResponse(
    val results: List<Video>
)

/**
 * Data class representing a video (trailer, teaser, clip) from TMDB API.
 *
 * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech/)
 *
 * @property key Unique identifier for the video on the hosting site (e.g., YouTube video ID)
 * @property site Video hosting site (e.g., "YouTube", "Vimeo")
 * @property type Video type (e.g., "Trailer", "Teaser", "Clip", "Behind the Scenes")
 * @property official Indicates if this is an official video from the studio/publisher
 *
 * @since 14 Nov 2025
 */
data class Video(
    val key: String,
    val site: String,
    val type: String,
    val official: Boolean
)