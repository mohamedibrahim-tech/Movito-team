package com.movito.movito.favorites

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import com.movito.movito.data.model.Movie
import java.util.Date

data class FavoriteMovie(
    @DocumentId
    val id: String = "",
    val movieId: Int = 0,
    val title: String = "",
    val releaseDate: String = "",
    val posterPath: String? = null,
    val voteAverage: Double = 0.0,
    val overview: String = "",
    val userId: String = "",
    @ServerTimestamp
    val addedAt: Date? = null
) {
    constructor() : this("", 0, "", "", null, 0.0, "", "", null)
}

fun Movie.toFavoriteMovie(userId: String): FavoriteMovie {
    return FavoriteMovie(
        id = "",
        movieId = id,
        title = title,
        releaseDate = releaseDate,
        posterPath = posterPath,
        voteAverage = voteAverage,
        overview = overview,
        userId = userId,
        addedAt = null
    )
}