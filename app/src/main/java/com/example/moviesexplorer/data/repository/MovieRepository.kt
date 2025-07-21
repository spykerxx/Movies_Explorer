package com.example.moviesexplorer.data.repository

import com.example.moviesexplorer.data.api.RetrofitInstance
import com.example.moviesexplorer.data.dto.MovieDto
import com.example.moviesexplorer.data.dto.VideoDto

class MovieRepository {

    // Use RetrofitInstance.api as the single source of MovieApi
    private val api = RetrofitInstance.api

    suspend fun searchMovies(apiKey: String, query: String): List<MovieDto> {
        if (query.isBlank()) return emptyList()
        val response = api.searchMovies(apiKey, query)
        return response.results
    }

    suspend fun getPopularMovies(apiKey: String): List<MovieDto> {
        val response = api.getPopularMovies(apiKey)
        return response.results
    }

    suspend fun getMovieVideos(movieId: Int, apiKey: String): List<VideoDto> {
        val response = api.getMovieVideos(movieId, apiKey)
        return response.results
    }
}