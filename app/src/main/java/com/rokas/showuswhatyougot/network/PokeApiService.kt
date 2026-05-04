package com.rokas.showuswhatyougot.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PokeApiService {
    @GET("pokemon")
    suspend fun getPokemonList(
        @Query("limit") limit: Int = 151,
        @Query("offset") offset: Int = 0,
    ): PokemonListResponse

    @GET("pokemon/{id}")
    suspend fun getPokemonDetail(
        @Path("id") id: Int,
    ): PokemonDetailResponse
}

data class PokemonListResponse(
    val results: List<PokemonListEntry>,
)

data class PokemonListEntry(
    val name: String,
    val url: String,
)

data class PokemonDetailResponse(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val abilities: List<PokemonAbilitySlotResponse>,
    val types: List<PokemonTypeSlotResponse>,
    val sprites: PokemonSpritesResponse,
)

data class PokemonAbilitySlotResponse(
    val ability: NamedApiResourceResponse,
)

data class PokemonTypeSlotResponse(
    val slot: Int,
    val type: NamedApiResourceResponse,
)

data class NamedApiResourceResponse(
    val name: String,
)

data class PokemonSpritesResponse(
    @SerializedName("front_default")
    val frontDefault: String?,
    val other: PokemonOtherSpritesResponse?,
)

data class PokemonOtherSpritesResponse(
    @SerializedName("official-artwork")
    val officialArtwork: PokemonArtworkResponse?,
)

data class PokemonArtworkResponse(
    @SerializedName("front_default")
    val frontDefault: String?,
)

