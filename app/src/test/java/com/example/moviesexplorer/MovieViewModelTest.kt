package com.example.moviesexplorer

import android.content.Context
import app.cash.turbine.test
import com.example.moviesexplorer.data.datastore.FavoritesDataStore
import com.example.moviesexplorer.data.dto.MovieDto
import com.example.moviesexplorer.data.repository.MovieRepository
import com.example.moviesexplorer.presentation.movie.MovieViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals


@ExperimentalCoroutinesApi
class MovieViewModelTest {

    @get:Rule
    val coroutineRule = MainDispatcherRule()

    private lateinit var movieRepository: MovieRepository
    private lateinit var favoritesDataStore: FavoritesDataStore
    private lateinit var context: Context
    private lateinit var movieViewModel: MovieViewModel

    @Before
    fun setup() {
        movieRepository = mockk(relaxed = true)
        context = mockk(relaxed = true)

        // Mock FavoritesDataStore with a flow returning empty set of favorites ids
        favoritesDataStore = mockk(relaxed = true)
        every { favoritesDataStore.favoritesIds } returns flowOf(emptySet())

        // Mock repository response for getPopularMovies
        coEvery { movieRepository.getPopularMovies(any()) } returns listOf(
            MovieDto(id = 1, title = "Inception", poster_path = null, vote_average = 8.8f),
            MovieDto(id = 2, title = "Interstellar", poster_path = null, vote_average = 8.6f)
        )

        // Construct ViewModel with mocks/fakes
        movieViewModel = MovieViewModel(movieRepository, favoritesDataStore)
    }

    @Test
    fun `movies are loaded successfully`() = runTest {
        movieViewModel.fetchPopularMovies("dummyApiKey")

        // Advance the dispatcher until all coroutines complete
        advanceUntilIdle()

        // Now test the flow
        movieViewModel.movies.test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals("Inception", result[0].title)
            cancelAndIgnoreRemainingEvents()
        }
    }




}
