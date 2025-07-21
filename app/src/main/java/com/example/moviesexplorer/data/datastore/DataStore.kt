package com.example.moviesexplorer.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.favoritesDataStore by preferencesDataStore(name = "favorites_prefs")

class FavoritesDataStore(private val context: Context) {
    companion object {
        private val FAVORITES_KEY = stringSetPreferencesKey("favorites_ids")
    }

    val favoritesIds: Flow<Set<String>> = context.favoritesDataStore.data.map {
        it[FAVORITES_KEY] ?: emptySet()
    }

    suspend fun saveFavoriteIds(ids: Set<String>) {
        context.favoritesDataStore.edit { prefs ->
            prefs[FAVORITES_KEY] = ids
        }
    }
}
