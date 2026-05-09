package com.rokas.showuswhatyougot.storage.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PokemonEntity::class, PokemonDetailEntity::class, FavoritePokemonEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class PokemonDatabase : RoomDatabase() {
    abstract fun pokemonDao(): PokemonDao
    abstract fun pokemonDetailDao(): PokemonDetailDao
    abstract fun favoritePokemonDao(): FavoritePokemonDao
}

