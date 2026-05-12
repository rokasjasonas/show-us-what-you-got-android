package com.rokas.showuswhatyougot.network.data

import com.rokas.showuswhatyougot.model.Pokemon
import com.rokas.showuswhatyougot.model.PokemonDetail
import com.rokas.showuswhatyougot.model.PokemonPage
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PokemonRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource,
) : com.rokas.showuswhatyougot.data.PokemonRepository {

    override suspend fun getPokemonPage(limit: Int, offset: Int): PokemonPage {
        val page = remoteDataSource.getPokemonPage(limit, offset)
        localDataSource.cachePokemonList(page.pokemon)
        return page
    }

    override suspend fun getPokemonDetail(id: Int): PokemonDetail {
        val detail = remoteDataSource.getPokemonDetail(id)
        localDataSource.cachePokemonDetail(detail)
        return detail
    }

    override suspend fun getCachedPokemonList(): List<Pokemon> {
        return localDataSource.getCachedPokemonList()
    }

    override suspend fun getCachedPokemonDetail(id: Int): PokemonDetail? {
        return localDataSource.getCachedPokemonDetail(id)
    }

    override fun getFavoritePokemon(): Flow<List<Pokemon>> {
        return localDataSource.getFavoritePokemon()
    }

    override fun isFavorite(pokemonId: Int): Flow<Boolean> {
        return localDataSource.isFavorite(pokemonId)
    }

    override fun getFavoriteIds(): Flow<Set<Int>> {
        return localDataSource.getFavoriteIds()
    }

    override suspend fun toggleFavorite(pokemonId: Int) {
        localDataSource.toggleFavorite(pokemonId)
    }
}
