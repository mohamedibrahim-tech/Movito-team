package com.movito.movito.data.model

/**
 *  نقلنا الـ data class هنا عشان يبقى مشترك
 */
data class Movie(
    val id: Int = 0,
    val title: String = "...",
    val year: String = "9999",
    val time: String = "99h 59m 59s",
    val posterUrl: String = "",
    val vote_avg: Float = 9.9f,
    val vote_count: Int = Int.MAX_VALUE,
    val production_countries: List<ProductionCountry> = listOf(),
    val spoken_languages: List<SpokenLanguage> = listOf(),
    val overview: String = "...",
    val genres: List<Genre> = listOf(),
    val homePage: String = "",

    )

data class ProductionCountry(
    val iso_3166_1: String,
    val name: String
)

data class SpokenLanguage(
    val iso_639_1: String,
    val name: String
)

data class Genre(
    val id: Int,
    val name: String
)

