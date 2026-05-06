package com.rokas.showuswhatyougot.feature.details

import com.rokas.showuswhatyougot.analytics.AnalyticsEngine
import com.rokas.showuswhatyougot.analytics.AnalyticsEvent
import com.rokas.showuswhatyougot.model.PokemonDetail
import com.rokas.showuswhatyougot.network.data.PokemonRepository
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PokemonDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository = mockk<PokemonRepository>()
    private val analyticsEngine = mockk<AnalyticsEngine>(relaxed = true)

    private lateinit var vm: PokemonDetailViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { repository.getCachedPokemonDetail(any()) } returns null
        vm = PokemonDetailViewModel(repository, analyticsEngine)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val detail = PokemonDetail(
        id = 25,
        name = "Pikachu",
        imageUrl = "url",
        types = listOf("Electric"),
        abilities = listOf("Static"),
        heightMeters = 0.4,
        weightKilograms = 6.0,
    )

    @Test
    fun `loadPokemon sets success state`() = runTest {
        coEvery { repository.getPokemonDetail(25) } returns detail

        vm.loadPokemon(25)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is PokemonDetailUiState.Success)
        assertEquals(detail, (state as PokemonDetailUiState.Success).pokemon)
    }

    @Test
    fun `loadPokemon sets error state on failure`() = runTest {
        coEvery { repository.getPokemonDetail(25) } throws RuntimeException("fail")

        vm.loadPokemon(25)
        advanceUntilIdle()

        assertTrue(vm.uiState.value is PokemonDetailUiState.Error)
    }

    @Test
    fun `loadPokemon shows cached data when network fails`() = runTest {
        coEvery { repository.getCachedPokemonDetail(25) } returns detail
        coEvery { repository.getPokemonDetail(25) } throws RuntimeException("fail")

        vm.loadPokemon(25)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is PokemonDetailUiState.Success)
        assertEquals(detail, (state as PokemonDetailUiState.Success).pokemon)
    }

    @Test
    fun `loadPokemon tracks analytics`() = runTest {
        coEvery { repository.getPokemonDetail(25) } returns detail

        vm.loadPokemon(25)
        advanceUntilIdle()

        verify { analyticsEngine.trackEvent(AnalyticsEvent.DetailsScreenOpen(25)) }
    }

    @Test
    fun `loadPokemon skips if same id already loaded`() = runTest {
        coEvery { repository.getPokemonDetail(25) } returns detail

        vm.loadPokemon(25)
        advanceUntilIdle()
        vm.loadPokemon(25)
        advanceUntilIdle()

        // Should only have been called once
        io.mockk.coVerify(exactly = 1) { repository.getPokemonDetail(25) }
    }

    @Test
    fun `retry reloads current pokemon`() = runTest {
        coEvery { repository.getPokemonDetail(25) } throws RuntimeException("fail")

        vm.loadPokemon(25)
        advanceUntilIdle()

        coEvery { repository.getPokemonDetail(25) } returns detail

        vm.retry()
        advanceUntilIdle()

        assertTrue(vm.uiState.value is PokemonDetailUiState.Success)
        verify { analyticsEngine.trackEvent(AnalyticsEvent.TryAgainClick) }
    }

    @Test
    fun `onNetworkRestored reloads on error state`() = runTest {
        coEvery { repository.getPokemonDetail(25) } throws RuntimeException("fail")

        vm.loadPokemon(25)
        advanceUntilIdle()

        coEvery { repository.getPokemonDetail(25) } returns detail

        vm.onNetworkRestored()
        advanceUntilIdle()

        assertTrue(vm.uiState.value is PokemonDetailUiState.Success)
    }

    @Test
    fun `onNetworkRestored does nothing on success state`() = runTest {
        coEvery { repository.getPokemonDetail(25) } returns detail

        vm.loadPokemon(25)
        advanceUntilIdle()

        vm.onNetworkRestored()
        advanceUntilIdle()

        // Still only called once
        io.mockk.coVerify(exactly = 1) { repository.getPokemonDetail(25) }
    }
}
