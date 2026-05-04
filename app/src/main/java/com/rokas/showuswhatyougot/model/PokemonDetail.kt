package com.rokas.showuswhatyougot.model

data class PokemonDetail(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val types: List<String>,
    val abilities: List<String>,
    val heightMeters: Double,
    val weightKilograms: Double,
)

