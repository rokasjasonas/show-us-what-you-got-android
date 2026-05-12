package com.rokas.showuswhatyougot.network.data

import com.rokas.showuswhatyougot.model.Pokemon
import com.rokas.showuswhatyougot.model.PokemonDetail
import com.rokas.showuswhatyougot.model.PokemonPage
import com.rokas.showuswhatyougot.network.PokeApiService
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteDataSource @Inject constructor(
    private val pokeApiService: PokeApiService,
) {
    companion object {
        private const val OFFICIAL_ARTWORK_URL =
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/%d.png"
    }

    suspend fun getPokemonPage(limit: Int, offset: Int): PokemonPage {
        val response = pokeApiService.getPokemonList(limit = limit, offset = offset)

        val pokemon = response.results.mapNotNull { entry ->
            val pokemonId = entry.url.trimEnd('/').substringAfterLast('/').toIntOrNull()
                ?: return@mapNotNull null
            Pokemon(
                id = pokemonId,
                name = entry.name.toDisplayName(),
                imageUrl = OFFICIAL_ARTWORK_URL.format(pokemonId),
            )
        }.sortedBy(Pokemon::id)

        return PokemonPage(
            pokemon = pokemon,
            nextOffset = response.next?.substringAfter("offset=")?.substringBefore('&')?.toIntOrNull(),
        )
    }

    suspend fun getPokemonDetail(id: Int): PokemonDetail {
        val response = pokeApiService.getPokemonDetail(id)

        return PokemonDetail(
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
    }

    private fun String.toDisplayName(): String =
        replace('-', ' ').split(' ').joinToString(" ") { part ->
            part.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase(Locale.ROOT) else char.toString()
            }
        }
}

