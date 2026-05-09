package com.rokas.showuswhatyougot.storage.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_pokemon")
data class FavoritePokemonEntity(
    @PrimaryKey val pokemonId: Int,
)

