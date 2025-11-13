package com.movito.movito.data.model

/**
 *  نقلنا الـ data class هنا عشان يبقى مشترك
 */
data class Movie(
    val id: Int,
    val title: String,
    val year: String,
    val time: String,
    val posterUrl: String,
    val vote_avg: String,
    val vote_count: String,
    val production_countries: List<ProductionCountry>,
    val spoken_languages: List<SpokenLanguage>,
    val overview: String,
    val genres: List<Genre>,
    val homePage: String,

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

