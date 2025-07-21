package com.example.moviesexplorer.presentation.movie

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviesexplorer.data.datastore.FavoritesDataStore
import com.example.moviesexplorer.data.dto.MovieDto
import com.example.moviesexplorer.data.dto.VideoDto
import com.example.moviesexplorer.data.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MovieViewModel(
    private val repository: MovieRepository,
    private val dataStore: FavoritesDataStore
) : ViewModel() {

    private val _movies = MutableStateFlow<List<MovieDto>>(emptyList())
    val movies: StateFlow<List<MovieDto>> = _movies

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    private val _favorites = MutableStateFlow<List<MovieDto>>(emptyList())
    val favorites: StateFlow<List<MovieDto>> = _favorites

    private val _movieVideos = MutableStateFlow<List<VideoDto>>(emptyList())
    val movieVideos: StateFlow<List<VideoDto>> = _movieVideos

    init {
        viewModelScope.launch {
            dataStore.favoritesIds.collect { savedIds ->
                val allMovies = _movies.value
                val favs = allMovies.filter { it.id.toString() in savedIds }
                _favorites.value = favs
            }
        }
    }

    fun toggleFavorite(movie: MovieDto) {
        val current = _favorites.value.toMutableList()
        val isAlreadyFavorite = current.any { it.id == movie.id }

        if (isAlreadyFavorite) {
            current.removeAll { it.id == movie.id }
        } else {
            current.add(movie)
        }

        _favorites.value = current

        viewModelScope.launch {
            val ids = current.map { it.id.toString() }.toSet()
            dataStore.saveFavoriteIds(ids)
        }
    }

    fun isFavorite(movieId: Int): Boolean {
        return _favorites.value.any { it.id == movieId }
    }

    fun fetchPopularMovies(apiKey: String) {
        viewModelScope.launch {
            _isSearching.value = false
            try {
                val result = repository.getPopularMovies(apiKey)
                _movies.value = result

                val savedIds = dataStore.favoritesIds.first()
                _favorites.value = result.filter { it.id.toString() in savedIds }
            } catch (e: Exception) {
                _movies.value = emptyList()
            }
        }
    }

    fun searchMovies(apiKey: String, query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                fetchPopularMovies(apiKey)
                return@launch
            }

            _isSearching.value = true
            try {
                val results = repository.searchMovies(apiKey, query)
                _movies.value = results
            } catch (e: Exception) {
                _movies.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun fetchMovieVideos(movieId: Int, apiKey: String) {
        viewModelScope.launch {
            try {
                val videos = repository.getMovieVideos(movieId, apiKey)
                _movieVideos.value = videos
            } catch (e: Exception) {
                _movieVideos.value = emptyList()
            }
        }
    }
}
