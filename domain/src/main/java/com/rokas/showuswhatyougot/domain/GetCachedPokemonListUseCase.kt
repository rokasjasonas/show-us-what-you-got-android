package com.rokas.showuswhatyougot.domain

import com.rokas.showuswhatyougot.data.PokemonRepository
import com.rokas.showuswhatyougot.model.Pokemon
import javax.inject.Inject

class GetCachedPokemonListUseCase @Inject constructor(
    private val repository: PokemonRepository,
) {
    suspend operator fun invoke(): List<Pokemon> {
        return repository.getCachedPokemonList()
    }
}

