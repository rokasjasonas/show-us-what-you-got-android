package com.rokas.showuswhatyougot.domain

import com.rokas.showuswhatyougot.data.PokemonRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveFavoriteIdsUseCase @Inject constructor(
    private val repository: PokemonRepository,
) {
    operator fun invoke(): Flow<Set<Int>> {
        return repository.getFavoriteIds()
    }
}

