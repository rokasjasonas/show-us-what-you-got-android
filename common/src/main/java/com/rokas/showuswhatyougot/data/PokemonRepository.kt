package com.rokas.showuswhatyougot.data

import com.rokas.showuswhatyougot.model.Pokemon
import com.rokas.showuswhatyougot.model.PokemonDetail
import com.rokas.showuswhatyougot.model.PokemonPage
import kotlinx.coroutines.flow.Flow

interface PokemonRepository {
    suspend fun getPokemonPage(limit: Int, offset: Int): PokemonPage
    suspend fun getPokemonDetail(id: Int): PokemonDetail
    suspend fun getCachedPokemonList(): List<Pokemon>
    suspend fun getCachedPokemonDetail(id: Int): PokemonDetail?
    fun getFavoritePokemon(): Flow<List<Pokemon>>
    fun isFavorite(pokemonId: Int): Flow<Boolean>
    fun getFavoriteIds(): Flow<Set<Int>>
    suspend fun toggleFavorite(pokemonId: Int)
}

