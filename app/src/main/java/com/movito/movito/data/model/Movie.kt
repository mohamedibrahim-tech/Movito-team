package com.movito.movito.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import android.app.Activity
import androidx.fragment.app.Fragment
import android.content.Intent

/**
 * Data class representing a movie from the TMDB API.
 *
 * This class serves as the primary data model for movie information throughout the app.
 * It implements [Parcelable] to support data transfer between Android components
 * ([Activity], [Fragment], [Intent]).
 *
 * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech/)
 *
 * @property id Unique identifier for the movie in TMDB database
 * @property title The movie's title
 * @property releaseDate Release date in `"YYYY-MM-DD"` format
 * @property posterPath Relative path to the movie's poster image (can be appended to TMDB image base URL)
 * @property voteAverage Average user rating (`0`-`10` scale)
 * @property overview Plot summary/synopsis
 * @property genreIds [List] of genre IDs associated with the movie
 * @property homepage Official website URL for the movie (may be empty)
 *
 * @see Genre for genre information model
 *
 * @since first appear in MoviesByGenreViewModel.kt file (8 Nov 2025), then moved to this file (13 Nov 2025)
 */
@Parcelize
data class Movie(
    @SerializedName("id")
    val id: Int = 1501271,

    @SerializedName("title")
    val title: String = "Cosmic Echoes",

    @SerializedName("release_date")
    val releaseDate: String = "2025-03-15",

    @SerializedName("poster_path")
    val posterPath: String? = "/3crxHloZ9ybnQiOv7GOTKhCcQKZ.jpg",

    @SerializedName("vote_average")
    val voteAverage: Double = 8.5,

    @SerializedName("overview")
    val overview: String = "An epic space opera.",

    @SerializedName("genre_ids")
    val genreIds: List<Int>? = emptyList(),

    @SerializedName("homepage")
    val homepage: String = ""
) : Parcelable

/**
 * Data class representing a movie genre from the TMDB API.
 *
 * This class implements [Parcelable] to support data transfer between Android components.
 *
 * **Author**: Movito Development Team Member [Yahia Mohamed](https://github.com/YahiaMohamed24)
 *
 * @property id Unique identifier for the genre in TMDB database
 * @property name Display name of the genre (localized based on API language parameter)
 *
 * @see Movie for the main movie model
 *
 * @since 13 Nov 2025
 */
@Parcelize
data class Genre(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String
) : Parcelable