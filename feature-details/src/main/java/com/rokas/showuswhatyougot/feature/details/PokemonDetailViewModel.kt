package com.rokas.showuswhatyougot.feature.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rokas.showuswhatyougot.analytics.AnalyticsEngine
import com.rokas.showuswhatyougot.analytics.AnalyticsEvent
import com.rokas.showuswhatyougot.network.data.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PokemonDetailViewModel @Inject constructor(
    private val pokemonRepository: PokemonRepository,
    private val analyticsEngine: AnalyticsEngine,
) : ViewModel() {

    private val _uiState = MutableStateFlow<PokemonDetailUiState>(PokemonDetailUiState.Loading)
    val uiState: StateFlow<PokemonDetailUiState> = _uiState.asStateFlow()

    private var currentPokemonId: Int? = null
    private var favoriteJob: Job? = null

    fun loadPokemon(pokemonId: Int) {
        if (currentPokemonId == pokemonId && _uiState.value !is PokemonDetailUiState.Error) return
        currentPokemonId = pokemonId
        analyticsEngine.trackEvent(AnalyticsEvent.DetailsScreenOpen(pokemonId))
        performLoad(pokemonId)
        observeFavorite(pokemonId)
    }

    fun toggleFavorite() {
        currentPokemonId?.let { id ->
            viewModelScope.launch {
                pokemonRepository.toggleFavorite(id)
            }
        }
    }

    private fun observeFavorite(pokemonId: Int) {
        favoriteJob?.cancel()
        favoriteJob = viewModelScope.launch {
            pokemonRepository.isFavorite(pokemonId).collect { isFav ->
                val current = _uiState.value
                if (current is PokemonDetailUiState.Success) {
                    _uiState.value = current.copy(isFavorite = isFav)
                }
            }
        }
    }

    fun retry() {
        analyticsEngine.trackEvent(AnalyticsEvent.TryAgainClick)
        currentPokemonId?.let { performLoad(it) }
    }

    fun onNetworkRestored() {
        if (_uiState.value is PokemonDetailUiState.Error) {
            currentPokemonId?.let { performLoad(it) }
        }
    }

    private fun performLoad(pokemonId: Int) {
        _uiState.value = PokemonDetailUiState.Loading
        viewModelScope.launch {
            val isFav = pokemonRepository.isFavorite(pokemonId).first()

            // Show cached data first
            val cached = pokemonRepository.getCachedPokemonDetail(pokemonId)
            if (cached != null) {
                _uiState.value = PokemonDetailUiState.Success(cached, isFavorite = isFav)
            }

            _uiState.value = try {
                PokemonDetailUiState.Success(pokemonRepository.getPokemonDetail(pokemonId), isFavorite = isFav)
            } catch (e: Exception) {
                if (cached != null) {
                    PokemonDetailUiState.Success(cached, isFavorite = isFav)
                } else {
                    PokemonDetailUiState.Error(e.message.orEmpty())
                }
            }
        }
    }
}
