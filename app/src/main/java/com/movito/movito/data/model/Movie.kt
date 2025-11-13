package com.movito.movito.data.model

import com.google.gson.annotations.SerializedName

/**
 *  نقلنا الـ data class هنا عشان يبقى مشترك
 */
data class Movie(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("release_date")
    val releaseDate: String,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("vote_average")
    val voteAverage: Double,
    @SerializedName("overview")
    val overview: String,
    @SerializedName("genre_ids")
    val genreIds: List<Int>? = emptyList(), //عيب:)
    @SerializedName("genres")
    val genres: List<Genre>? = emptyList()
)

data class Genre(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String
)
