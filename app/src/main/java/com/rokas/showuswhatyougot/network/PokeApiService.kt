package com.rokas.showuswhatyougot.network

import retrofit2.http.GET
import retrofit2.http.Query

interface PokeApiService {
    @GET("pokemon")
    suspend fun getPokemonList(
        @Query("limit") limit: Int = 151,
        @Query("offset") offset: Int = 0,
    ): PokemonListResponse
}

data class PokemonListResponse(
    val results: List<PokemonListEntry>,
)

data class PokemonListEntry(
    val name: String,
    val url: String,
)

