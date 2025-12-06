package com.movito.movito.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 *  نقلنا الـ data class هنا عشان يبقى مشترك
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

@Parcelize
data class Genre(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String
) : Parcelable