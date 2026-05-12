package com.rokas.showuswhatyougot.network.data

import com.rokas.showuswhatyougot.model.Pokemon
import com.rokas.showuswhatyougot.model.PokemonDetail
import com.rokas.showuswhatyougot.model.PokemonPage
import com.rokas.showuswhatyougot.network.PokeApiService
import com.rokas.showuswhatyougot.storage.db.FavoritePokemonDao
import com.rokas.showuswhatyougot.storage.db.FavoritePokemonEntity
import com.rokas.showuswhatyougot.storage.db.PokemonDao
import com.rokas.showuswhatyougot.storage.db.PokemonDetailDao
import com.rokas.showuswhatyougot.storage.db.PokemonDetailEntity
import com.rokas.showuswhatyougot.storage.db.PokemonEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PokemonRepositoryImpl @Inject constructor(
    private val pokeApiService: PokeApiService,
    private val pokemonDao: PokemonDao,
    private val pokemonDetailDao: PokemonDetailDao,
    private val favoritePokemonDao: FavoritePokemonDao,
) : com.rokas.showuswhatyougot.data.PokemonRepository {
    companion object {
        private const val OFFICIAL_ARTWORK_URL =
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/%d.png"
    }

    fun getPokemonPageFlow(
        limit: Int,
        offset: Int,
    ): Flow<PokemonPage> = flow {
        // Emit cached data first
        val cached = pokemonDao.getAll()
        if (cached.isNotEmpty()) {
            emit(
                PokemonPage(
                    pokemon = cached.map { it.toPokemon() },
                    nextOffset = offset, // signal that more can be loaded
                )
            )
        }

        // Fetch from network
        val response = pokeApiService.getPokemonList(limit = limit, offset = offset)
        val networkPokemon = response.results.mapNotNull { entry ->
            val pokemonId = entry.url.trimEnd('/').substringAfterLast('/').toIntOrNull() ?: return@mapNotNull null
            Pokemon(
                id = pokemonId,
                name = entry.name.toDisplayName(),
                imageUrl = OFFICIAL_ARTWORK_URL.format(pokemonId),
            )
        }.sortedBy(Pokemon::id)

        // Cache the results
        pokemonDao.insertAll(networkPokemon.map { it.toEntity() })

        emit(
            PokemonPage(
                pokemon = networkPokemon,
                nextOffset = response.next?.substringAfter("offset=")?.substringBefore('&')?.toIntOrNull(),
            )
        )
    }

    fun getPokemonDetailFlow(id: Int): Flow<PokemonDetail> = flow {
        // Emit cached data first
        val cached = pokemonDetailDao.getById(id)
        if (cached != null) {
            emit(cached.toPokemonDetail())
        }

        // Fetch from network
        val response = pokeApiService.getPokemonDetail(id)
        val detail = PokemonDetail(
            id = response.id,
            name = response.name.toDisplayName(),
            imageUrl = response.sprites.other?.officialArtwork?.frontDefault
                ?: response.sprites.frontDefault
                ?: OFFICIAL_ARTWORK_URL.format(response.id),
            types = response.types
                .sortedBy { it.slot }
                .map { it.type.name.toDisplayName() },
            abilities = response.abilities
                .map { it.ability.name.toDisplayName() }
                .sorted(),
            heightMeters = response.height / 10.0,
            weightKilograms = response.weight / 10.0,
        )

        // Cache the result
        pokemonDetailDao.insert(detail.toEntity())

        emit(detail)
    }

    // Keep original suspend functions for backward compat
    override suspend fun getPokemonPage(
        limit: Int,
        offset: Int,
    ): PokemonPage {
        val response = pokeApiService.getPokemonList(
            limit = limit,
            offset = offset,
        )

        val networkPokemon = response.results.mapNotNull { entry ->
            val pokemonId = entry.url.trimEnd('/').substringAfterLast('/').toIntOrNull() ?: return@mapNotNull null
            Pokemon(
                id = pokemonId,
                name = entry.name.toDisplayName(),
                imageUrl = OFFICIAL_ARTWORK_URL.format(pokemonId),
            )
        }.sortedBy(Pokemon::id)

        pokemonDao.insertAll(networkPokemon.map { it.toEntity() })

        return PokemonPage(
            pokemon = networkPokemon,
            nextOffset = response.next?.substringAfter("offset=")?.substringBefore('&')?.toIntOrNull(),
        )
    }

    override suspend fun getPokemonDetail(id: Int): PokemonDetail {
        val response = pokeApiService.getPokemonDetail(id)

        val detail = PokemonDetail(
            id = response.id,
            name = response.name.toDisplayName(),
            imageUrl = response.sprites.other?.officialArtwork?.frontDefault
                ?: response.sprites.frontDefault
                ?: OFFICIAL_ARTWORK_URL.format(response.id),
            types = response.types
                .sortedBy { it.slot }
                .map { it.type.name.toDisplayName() },
            abilities = response.abilities
                .map { it.ability.name.toDisplayName() }
                .sorted(),
            heightMeters = response.height / 10.0,
            weightKilograms = response.weight / 10.0,
        )

        pokemonDetailDao.insert(detail.toEntity())

        return detail
    }

    override suspend fun getCachedPokemonList(): List<Pokemon> {
        return pokemonDao.getAll().map { it.toPokemon() }
    }

    override suspend fun getCachedPokemonDetail(id: Int): PokemonDetail? {
        return pokemonDetailDao.getById(id)?.toPokemonDetail()
    }

    override fun getFavoritePokemon(): Flow<List<Pokemon>> {
        return favoritePokemonDao.getAll().map { favorites ->
            val ids = favorites.map { it.pokemonId }.toSet()
            if (ids.isEmpty()) emptyList()
            else pokemonDao.getAll()
                .filter { it.id in ids }
                .map { it.toPokemon() }
        }
    }

    override fun isFavorite(pokemonId: Int): Flow<Boolean> = favoritePokemonDao.isFavorite(pokemonId)

    override fun getFavoriteIds(): Flow<Set<Int>> = favoritePokemonDao.getAll().map { list ->
        list.map { it.pokemonId }.toSet()
    }

    override suspend fun toggleFavorite(pokemonId: Int) {
        val entity = FavoritePokemonEntity(pokemonId)
        // Use a simple check-and-toggle
        val isFav = favoritePokemonDao.isFavorite(pokemonId).first()
        if (isFav) {
            favoritePokemonDao.delete(pokemonId)
        } else {
            favoritePokemonDao.insert(entity)
        }
    }

    private fun String.toDisplayName(): String =
        replace('-', ' ').split(' ').joinToString(" ") { part ->
            part.replaceFirstChar { char ->
                if (char.isLowerCase()) {
                    char.titlecase(Locale.ROOT)
                } else {
                    char.toString()
                }
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

