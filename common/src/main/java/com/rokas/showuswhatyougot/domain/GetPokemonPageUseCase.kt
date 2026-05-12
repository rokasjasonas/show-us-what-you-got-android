package com.rokas.showuswhatyougot.domain

import com.rokas.showuswhatyougot.data.PokemonRepository
import com.rokas.showuswhatyougot.model.PokemonPage
import javax.inject.Inject

class GetPokemonPageUseCase @Inject constructor(
    private val repository: PokemonRepository,
) {
    suspend operator fun invoke(limit: Int, offset: Int): PokemonPage {
        return repository.getPokemonPage(limit, offset)
    }
}

