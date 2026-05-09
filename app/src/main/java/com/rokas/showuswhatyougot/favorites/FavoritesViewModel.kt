package com.rokas.showuswhatyougot.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rokas.showuswhatyougot.model.Pokemon
import com.rokas.showuswhatyougot.network.data.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val pokemonRepository: PokemonRepository,
) : ViewModel() {

    val favorites: StateFlow<List<Pokemon>> = pokemonRepository.getFavoritePokemon()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleFavorite(pokemonId: Int) {
        viewModelScope.launch {
            pokemonRepository.toggleFavorite(pokemonId)
        }
    }
}

