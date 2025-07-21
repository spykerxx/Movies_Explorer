package com.example.moviesexplorer.presentation.movie

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.moviesexplorer.data.repository.MovieRepository
import com.example.moviesexplorer.data.datastore.FavoritesDataStore

class MovieViewModelFactory(
    private val repository: MovieRepository,
    private val dataStore: FavoritesDataStore
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MovieViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MovieViewModel(repository, dataStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
