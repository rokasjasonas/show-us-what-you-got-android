package com.rokas.showuswhatyougot.domain

import com.rokas.showuswhatyougot.data.PokemonRepository
import com.rokas.showuswhatyougot.model.Pokemon
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFavoritePokemonUseCase @Inject constructor(
    private val repository: PokemonRepository,
) {
    operator fun invoke(): Flow<List<Pokemon>> {
        return repository.getFavoritePokemon()
    }
}

