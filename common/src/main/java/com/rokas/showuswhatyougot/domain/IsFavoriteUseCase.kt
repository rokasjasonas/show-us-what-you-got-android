package com.rokas.showuswhatyougot.domain

import com.rokas.showuswhatyougot.data.PokemonRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class IsFavoriteUseCase @Inject constructor(
    private val repository: PokemonRepository,
) {
    operator fun invoke(pokemonId: Int): Flow<Boolean> {
        return repository.isFavorite(pokemonId)
    }
}

