package com.rokas.showuswhatyougot.data

import com.google.gson.GsonBuilder
import com.rokas.showuswhatyougot.model.Pokemon
import com.rokas.showuswhatyougot.network.PokeApiService
import java.util.Locale
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object PokemonRepository {
    private const val BASE_URL = "https://pokeapi.co/api/v2/"
    private const val OFFICIAL_ARTWORK_URL =
        "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/%d.png"

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
        .build()

    private val pokeApiService: PokeApiService = retrofit.create(PokeApiService::class.java)

    suspend fun getPokemon(limit: Int = 151): List<Pokemon> =
        pokeApiService.getPokemonList(limit = limit).results.mapNotNull { entry ->
            val pokemonId = entry.url.trimEnd('/').substringAfterLast('/').toIntOrNull() ?: return@mapNotNull null
            Pokemon(
                id = pokemonId,
                name = entry.name.toDisplayName(),
                imageUrl = OFFICIAL_ARTWORK_URL.format(pokemonId),
            )
        }.sortedBy(Pokemon::id)

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
}

