package com.movito.movito.data.source.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Singleton object responsible for creating and providing a Retrofit instance for TMDB API communication.
 *
 * This object follows the lazy initialization pattern to ensure the Retrofit instance is created
 * only when first accessed. It configures:
 * - Base URL for TMDB API v3
 * - Gson converter for JSON serialization/deserialization
 *
 * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech/)
 *
 * @property api Lazy-initialized Retrofit service implementing [TmdbApi] interface
 *
 * @see TmdbApi for the API interface definition
 * @see GsonConverterFactory for JSON conversion
 *
 * @since 13 Nov 2025
 */
object RetrofitInstance {

    /**
     * Base URL for TMDB API version 3.
     * This endpoint provides access to all movie-related data including:
     * - Movie discovery and search
     * - Genre information
     * - Movie videos (trailers)
     * - Recommendations
     */
    private const val BASE_URL = "https://api.themoviedb.org/3/"

    /**
     * Lazy-initialized Retrofit service instance.
     *
     * This property uses Kotlin's `by lazy` delegate to ensure:
     * - Thread-safe initialization
     * - Single instance creation (singleton pattern)
     * - Memory efficiency (only created when needed)
     *
     * The Retrofit instance is configured with:
     * 1. [BASE_URL] as the base endpoint
     * 2. [GsonConverterFactory] for JSON parsing
     *
     * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech/)
     *
     * @throws IllegalStateException if Retrofit fails to create the API instance
     *
     * @since 13 Nov 2025
     */
    val api: TmdbApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TmdbApi::class.java)
    }
}