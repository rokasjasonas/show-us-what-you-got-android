package com.rokas.showuswhatyougot.storage.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pokemon_detail")
data class PokemonDetailEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val imageUrl: String,
    val types: String, // comma-separated
    val abilities: String, // comma-separated
    val heightMeters: Double,
    val weightKilograms: Double,
)

