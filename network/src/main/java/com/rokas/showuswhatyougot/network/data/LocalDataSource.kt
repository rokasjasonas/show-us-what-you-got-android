package com.rokas.showuswhatyougot.network.data

import com.rokas.showuswhatyougot.model.Pokemon
import com.rokas.showuswhatyougot.model.PokemonDetail
import com.rokas.showuswhatyougot.storage.db.FavoritePokemonDao
import com.rokas.showuswhatyougot.storage.db.FavoritePokemonEntity
import com.rokas.showuswhatyougot.storage.db.PokemonDao
import com.rokas.showuswhatyougot.storage.db.PokemonDetailDao
import com.rokas.showuswhatyougot.storage.db.PokemonDetailEntity
import com.rokas.showuswhatyougot.storage.db.PokemonEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDataSource @Inject constructor(
    private val pokemonDao: PokemonDao,
    private val pokemonDetailDao: PokemonDetailDao,
    private val favoritePokemonDao: FavoritePokemonDao,
) {
    suspend fun getCachedPokemonList(): List<Pokemon> {
        return pokemonDao.getAll().map { it.toPokemon() }
    }

    suspend fun cachePokemonList(pokemon: List<Pokemon>) {
        pokemonDao.insertAll(pokemon.map { it.toEntity() })
    }

    suspend fun getCachedPokemonDetail(id: Int): PokemonDetail? {
        return pokemonDetailDao.getById(id)?.toPokemonDetail()
    }

    suspend fun cachePokemonDetail(detail: PokemonDetail) {
        pokemonDetailDao.insert(detail.toEntity())
    }

    fun getFavoritePokemon(): Flow<List<Pokemon>> {
        return favoritePokemonDao.getAll().map { favorites ->
            val ids = favorites.map { it.pokemonId }.toSet()
            if (ids.isEmpty()) emptyList()
            else pokemonDao.getAll()
                .filter { it.id in ids }
                .map { it.toPokemon() }
        }
    }

    fun isFavorite(pokemonId: Int): Flow<Boolean> = favoritePokemonDao.isFavorite(pokemonId)

    fun getFavoriteIds(): Flow<Set<Int>> = favoritePokemonDao.getAll().map { list ->
        list.map { it.pokemonId }.toSet()
    }

    suspend fun toggleFavorite(pokemonId: Int) {
        val isFav = favoritePokemonDao.isFavorite(pokemonId).first()
        if (isFav) {
            favoritePokemonDao.delete(pokemonId)
        } else {
            favoritePokemonDao.insert(FavoritePokemonEntity(pokemonId))
        }
    }

    private fun PokemonEntity.toPokemon() = Pokemon(id = id, name = name, imageUrl = imageUrl)

    private fun Pokemon.toEntity() = PokemonEntity(id = id, name = name, imageUrl = imageUrl)

    private fun PokemonDetailEntity.toPokemonDetail() = PokemonDetail(
        id = id,
        name = name,
        imageUrl = imageUrl,
        types = types.split(",").filter { it.isNotEmpty() },
        abilities = abilities.split(",").filter { it.isNotEmpty() },
        heightMeters = heightMeters,
        weightKilograms = weightKilograms,
    )

    private fun PokemonDetail.toEntity() = PokemonDetailEntity(
        id = id,
        name = name,
        imageUrl = imageUrl,
        types = types.joinToString(","),
        abilities = abilities.joinToString(","),
        heightMeters = heightMeters,
        weightKilograms = weightKilograms,
    )
}

