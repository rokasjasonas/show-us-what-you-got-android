package com.rokas.showuswhatyougot.feature.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rokas.showuswhatyougot.analytics.AnalyticsEngine
import com.rokas.showuswhatyougot.analytics.AnalyticsEvent
import com.rokas.showuswhatyougot.network.data.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PAGE_SIZE = 30

@HiltViewModel
class PokemonListViewModel @Inject constructor(
    private val pokemonRepository: PokemonRepository,
    private val analyticsEngine: AnalyticsEngine,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PokemonUiState(isInitialLoading = true, nextOffset = 0)
    )
    val uiState: StateFlow<PokemonUiState> = _uiState.asStateFlow()

    init {
        loadInitialPage()
        observeFavorites()
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            pokemonRepository.getFavoriteIds().collect { ids ->
                _uiState.value = _uiState.value.copy(favoriteIds = ids)
            }
        }
    }

    fun toggleFavorite(pokemonId: Int) {
        viewModelScope.launch {
            pokemonRepository.toggleFavorite(pokemonId)
        }
    }

    fun loadInitialPage() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isInitialLoading = true, nextOffset = 0, pokemon = emptyList(), initialErrorMessage = "", appendErrorMessage = "")

            // Show cached data first
            val cached = pokemonRepository.getCachedPokemonList()
            if (cached.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(pokemon = cached)
            }

            _uiState.value = try {
                val page = pokemonRepository.getPokemonPage(limit = PAGE_SIZE, offset = 0)
                _uiState.value.copy(pokemon = page.pokemon, nextOffset = page.nextOffset, isInitialLoading = false)
            } catch (e: Exception) {
                if (cached.isNotEmpty()) {
                    _uiState.value.copy(pokemon = cached, nextOffset = 0, isInitialLoading = false)
                } else {
                    _uiState.value.copy(initialErrorMessage = e.message.orEmpty(), nextOffset = 0, isInitialLoading = false)
                }
            }
        }
    }

    fun loadNextPage() {
        val current = _uiState.value
        val nextOffset = current.nextOffset ?: return
        if (current.isInitialLoading || current.isAppending) return

        _uiState.value = current.copy(isAppending = true, appendErrorMessage = "")

        viewModelScope.launch {
            _uiState.value = try {
                val page = pokemonRepository.getPokemonPage(limit = PAGE_SIZE, offset = nextOffset)
                _uiState.value.copy(
                    pokemon = current.pokemon + page.pokemon,
                    isAppending = false,
                    appendErrorMessage = "",
                    nextOffset = page.nextOffset,
                )
            } catch (e: Exception) {
                _uiState.value.copy(isAppending = false, appendErrorMessage = e.message.orEmpty())
            }
        }
    }

    fun retry() {
        analyticsEngine.trackEvent(AnalyticsEvent.TryAgainClick)
        val current = _uiState.value
        if (current.initialErrorMessage.isNotEmpty()) {
            loadInitialPage()
        } else if (current.appendErrorMessage.isNotEmpty()) {
            loadNextPage()
        }
    }

    fun onPokemonClick(pokemonId: Int) {
        analyticsEngine.trackEvent(AnalyticsEvent.PokemonClick(pokemonId))
    }

    fun onNetworkRestored() {
        val current = _uiState.value
        if (current.initialErrorMessage.isNotEmpty()) {
            loadInitialPage()
        } else if (current.appendErrorMessage.isNotEmpty()) {
            loadNextPage()
        }
    }

    fun onScreenOpened() {
        analyticsEngine.trackEvent(AnalyticsEvent.HomeScreenOpen)
    }
}

