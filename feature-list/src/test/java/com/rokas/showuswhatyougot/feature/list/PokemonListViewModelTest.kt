package com.rokas.showuswhatyougot.feature.list

import com.rokas.showuswhatyougot.analytics.AnalyticsEngine
import com.rokas.showuswhatyougot.analytics.AnalyticsEvent
import com.rokas.showuswhatyougot.model.Pokemon
import com.rokas.showuswhatyougot.network.data.PokemonPage
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PokemonListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository = mockk<PokemonRepository>()
    private val analyticsEngine = mockk<AnalyticsEngine>(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): PokemonListViewModel {
        return PokemonListViewModel(repository, analyticsEngine)
    }

    @Test
    fun `init loads first page successfully`() = runTest {
        val pokemon = listOf(Pokemon(1, "Bulbasaur", "url"))
        coEvery { repository.getPokemonPage(30, 0) } returns PokemonPage(pokemon, 30)

        val vm = createViewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(pokemon, state.pokemon)
        assertEquals(30, state.nextOffset)
        assertFalse(state.isInitialLoading)
    }

    @Test
    fun `init sets error on failure`() = runTest {
        coEvery { repository.getPokemonPage(30, 0) } throws RuntimeException("Network error")

        val vm = createViewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("Network error", state.initialErrorMessage)
        assertTrue(state.pokemon.isEmpty())
    }

    @Test
    fun `loadNextPage appends pokemon`() = runTest {
        val page1 = listOf(Pokemon(1, "Bulbasaur", "url"))
        val page2 = listOf(Pokemon(2, "Ivysaur", "url2"))
        coEvery { repository.getPokemonPage(30, 0) } returns PokemonPage(page1, 30)
        coEvery { repository.getPokemonPage(30, 30) } returns PokemonPage(page2, 60)

        val vm = createViewModel()
        advanceUntilIdle()

        vm.loadNextPage()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(2, state.pokemon.size)
        assertEquals(60, state.nextOffset)
    }

    @Test
    fun `loadNextPage does nothing when no next offset`() = runTest {
        coEvery { repository.getPokemonPage(30, 0) } returns PokemonPage(
            listOf(Pokemon(1, "Bulbasaur", "url")), null
        )

        val vm = createViewModel()
        advanceUntilIdle()

        vm.loadNextPage()
        advanceUntilIdle()

        assertEquals(1, vm.uiState.value.pokemon.size)
    }

    @Test
    fun `retry reloads initial page on initial error`() = runTest {
        coEvery { repository.getPokemonPage(30, 0) } throws RuntimeException("fail")

        val vm = createViewModel()
        advanceUntilIdle()

        val pokemon = listOf(Pokemon(1, "Bulbasaur", "url"))
        coEvery { repository.getPokemonPage(30, 0) } returns PokemonPage(pokemon, 30)

        vm.retry()
        advanceUntilIdle()

        assertEquals(pokemon, vm.uiState.value.pokemon)
        verify { analyticsEngine.trackEvent(AnalyticsEvent.TryAgainClick) }
    }

    @Test
    fun `onPokemonClick tracks analytics`() = runTest {
        coEvery { repository.getPokemonPage(30, 0) } returns PokemonPage(emptyList(), null)

        val vm = createViewModel()
        advanceUntilIdle()

        vm.onPokemonClick(25)

        verify { analyticsEngine.trackEvent(AnalyticsEvent.PokemonClick(25)) }
    }

    @Test
    fun `onScreenOpened tracks analytics`() = runTest {
        coEvery { repository.getPokemonPage(30, 0) } returns PokemonPage(emptyList(), null)

        val vm = createViewModel()
        advanceUntilIdle()

        vm.onScreenOpened()

        verify { analyticsEngine.trackEvent(AnalyticsEvent.HomeScreenOpen) }
    }

    @Test
    fun `onNetworkRestored reloads on initial error`() = runTest {
        coEvery { repository.getPokemonPage(30, 0) } throws RuntimeException("fail")

        val vm = createViewModel()
        advanceUntilIdle()

        val pokemon = listOf(Pokemon(1, "Bulbasaur", "url"))
        coEvery { repository.getPokemonPage(30, 0) } returns PokemonPage(pokemon, 30)

        vm.onNetworkRestored()
        advanceUntilIdle()

        assertEquals(pokemon, vm.uiState.value.pokemon)
    }
}

