package com.rokas.showuswhatyougot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rokas.showuswhatyougot.analytics.AnalyticsEngine
import com.rokas.showuswhatyougot.analytics.AnalyticsEvent
import com.rokas.showuswhatyougot.data.PokemonRepository
import com.rokas.showuswhatyougot.feature.list.PokemonUiState
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
    }

    fun loadInitialPage() {
        viewModelScope.launch {
            _uiState.value = PokemonUiState(isInitialLoading = true, nextOffset = 0)
            _uiState.value = try {
                val page = pokemonRepository.getPokemonPage(limit = PAGE_SIZE, offset = 0)
                PokemonUiState(pokemon = page.pokemon, nextOffset = page.nextOffset)
            } catch (e: Exception) {
                PokemonUiState(initialErrorMessage = e.message.orEmpty(), nextOffset = 0)
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
                current.copy(
                    pokemon = current.pokemon + page.pokemon,
                    isAppending = false,
                    appendErrorMessage = "",
                    nextOffset = page.nextOffset,
                )
            } catch (e: Exception) {
                current.copy(isAppending = false, appendErrorMessage = e.message.orEmpty())
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

