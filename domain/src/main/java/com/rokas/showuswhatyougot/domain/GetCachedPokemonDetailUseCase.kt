package com.rokas.showuswhatyougot.domain

import com.rokas.showuswhatyougot.data.PokemonRepository
import com.rokas.showuswhatyougot.model.PokemonDetail
import javax.inject.Inject

class GetCachedPokemonDetailUseCase @Inject constructor(
    private val repository: PokemonRepository,
) {
    suspend operator fun invoke(id: Int): PokemonDetail? {
        return repository.getCachedPokemonDetail(id)
    }
}

