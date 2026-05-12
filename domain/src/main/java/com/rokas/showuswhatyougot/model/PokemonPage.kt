package com.rokas.showuswhatyougot.model

data class PokemonPage(
    val pokemon: List<Pokemon>,
    val nextOffset: Int?,
)

