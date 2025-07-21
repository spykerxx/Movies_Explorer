package com.example.moviesexplorer.data.dto

data class MovieDto(
    val id: Int,
    val title: String,
    val poster_path: String?,
    val vote_average: Float,
    val overview: String? = null,
    val release_date: String? = null
)