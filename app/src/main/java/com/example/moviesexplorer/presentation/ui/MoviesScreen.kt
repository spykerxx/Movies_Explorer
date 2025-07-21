package com.example.moviesexplorer.presentation.ui

import android.annotation.SuppressLint
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.moviesexplorer.data.dto.MovieDto
import com.example.moviesexplorer.presentation.movie.MovieViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieExplorerScreen(
    viewModel: MovieViewModel = viewModel(),
    onMovieClick: (MovieDto) -> Unit,
    movies: List<MovieDto>? = null,
    isSearching: Boolean? = null,
) {
    val realMovies by viewModel.movies.collectAsState()
    val realIsSearching by viewModel.isSearching.collectAsState()

    val displayMovies = movies ?: realMovies
    val displayIsSearching = isSearching ?: realIsSearching
    var searchQuery by remember { mutableStateOf("") }
    val apiKey = "ca175e03fddb689c6b11544b52607154"

    LaunchedEffect(Unit) {
        if (movies == null) {
            viewModel.fetchPopularMovies(apiKey)
        }
    }
    LaunchedEffect(searchQuery) {
        if (movies == null) {
            viewModel.searchMovies(apiKey, searchQuery)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Movie Explorer") })
        },
        content = { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search movies...") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    singleLine = true
                )

                Text(
                    text = if (searchQuery.isBlank()) "Popular Movies" else "Search Results",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (displayIsSearching) {
                    CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                } else if (displayMovies.isEmpty()) {
                    Text("No results found", Modifier.align(Alignment.CenterHorizontally))
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(displayMovies) { movie ->
                            MovieCard(
                                movie = movie,
                                onClick = { onMovieClick(movie) },
                                isFavorite = viewModel.isFavorite(movie.id),
                                onFavoriteToggle = { viewModel.toggleFavorite(movie) }
                            )
                        }
                    }
                }
            }
        }
    )
}


@Composable
fun MovieCard(
    movie: MovieDto,
    onClick: () -> Unit,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit
) {
    val posterUrl = "https://image.tmdb.org/t/p/w500${movie.poster_path ?: ""}"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(2f / 3f)
            .padding(4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = posterUrl,
                contentDescription = movie.title,
                modifier = Modifier.fillMaxSize()
            )

            // Favorite IconButton (top right)
            IconButton(
                onClick = { onFavoriteToggle() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (isFavorite) "Unfavorite" else "Favorite",
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else Color.White
                )
            }


        }
    }
}


@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailsScreen(movieId: Int, viewModel: MovieViewModel, navController: NavController) {
    val movies by viewModel.movies.collectAsState()
    val movie = movies.find { it.id == movieId }

    val videos by viewModel.movieVideos.collectAsState()
    val scrollState = rememberScrollState()
    val apiKey = "ca175e03fddb689c6b11544b52607154"  // Replace with your real API key

    // Trigger fetch videos when movieId changes
    LaunchedEffect(movieId) {
        viewModel.fetchMovieVideos(movieId, apiKey)
    }

    // Find first YouTube trailer video (outside Column so we can reuse)
    val trailer = videos.firstOrNull {
        it.site.equals("YouTube", ignoreCase = true) && it.type.equals("Trailer", ignoreCase = true)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(movie?.title ?: "Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { padding ->
            if (movie == null) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Movie not found")
                }
            } else {
                Column(
                    modifier = Modifier
                        .verticalScroll(scrollState)
                        .padding(padding)
                        .padding(16.dp)
                        .fillMaxSize()
                ) {
                    val posterUrl = "https://image.tmdb.org/t/p/w780${movie.poster_path ?: ""}"

                    AsyncImage(
                        model = posterUrl,
                        contentDescription = movie.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 450.dp)
                            .clip(MaterialTheme.shapes.medium),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(Modifier.height(24.dp))

                    Text(
                        movie.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Text(
                        text = movie.overview ?: "No description available.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⭐ Rating: ${movie.vote_average}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "📅 Release: ${movie.release_date ?: "Unknown"}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    if (trailer != null) {
                        // Embed YouTube player using WebView
                        Text("Trailer:", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        AndroidView(factory = { context ->
                            WebView(context).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.cacheMode = WebSettings.LOAD_DEFAULT
                                webViewClient = WebViewClient()
                                loadUrl("https://www.youtube.com/embed/${trailer.key}?autoplay=0&modestbranding=1")
                            }
                        }, modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(MaterialTheme.shapes.medium)
                        )
                    } else {
                        Text("Trailer not available", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    )
}


