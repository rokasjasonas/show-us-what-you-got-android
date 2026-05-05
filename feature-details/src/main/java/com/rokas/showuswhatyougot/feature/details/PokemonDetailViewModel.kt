package com.rokas.showuswhatyougot.feature.details

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

@HiltViewModel
class PokemonDetailViewModel @Inject constructor(
    private val pokemonRepository: PokemonRepository,
    private val analyticsEngine: AnalyticsEngine,
) : ViewModel() {

    private val _uiState = MutableStateFlow<PokemonDetailUiState>(PokemonDetailUiState.Loading)
    val uiState: StateFlow<PokemonDetailUiState> = _uiState.asStateFlow()

    private var currentPokemonId: Int? = null

    fun loadPokemon(pokemonId: Int) {
        if (currentPokemonId == pokemonId && _uiState.value !is PokemonDetailUiState.Error) return
        currentPokemonId = pokemonId
        analyticsEngine.trackEvent(AnalyticsEvent.DetailsScreenOpen(pokemonId))
        performLoad(pokemonId)
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
            _uiState.value = try {
                PokemonDetailUiState.Success(pokemonRepository.getPokemonDetail(pokemonId))
            } catch (e: Exception) {
                PokemonDetailUiState.Error(e.message.orEmpty())
            }
        }
    }
}

