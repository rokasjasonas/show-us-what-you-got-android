package com.rokas.showuswhatyougot.feature.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rokas.showuswhatyougot.domain.GetFavoritePokemonUseCase
import com.rokas.showuswhatyougot.domain.ToggleFavoriteUseCase
import com.rokas.showuswhatyougot.model.Pokemon
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    getFavoritePokemon: GetFavoritePokemonUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
) : ViewModel() {

    val favorites: StateFlow<List<Pokemon>> = getFavoritePokemon()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleFavorite(pokemonId: Int) {
        viewModelScope.launch {
            toggleFavoriteUseCase(pokemonId)
        }
    }
}
