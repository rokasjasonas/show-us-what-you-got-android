package com.rokas.showuswhatyougot.domain

import com.rokas.showuswhatyougot.data.PokemonRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val repository: PokemonRepository,
) {
    suspend operator fun invoke(pokemonId: Int) {
        repository.toggleFavorite(pokemonId)
    }
}

