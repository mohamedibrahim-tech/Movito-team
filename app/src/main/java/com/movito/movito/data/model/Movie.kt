package com.movito.movito.data.model

/**
 *  نقلنا الـ data class هنا عشان يبقى مشترك
 */
data class Movie(
    val id: Int,
    val title: String,
    val year: String,
    val time: String,
    val posterUrl: String 
)