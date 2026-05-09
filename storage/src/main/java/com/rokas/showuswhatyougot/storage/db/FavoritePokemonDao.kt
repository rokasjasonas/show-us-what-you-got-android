package com.rokas.showuswhatyougot.storage.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritePokemonDao {
    @Query("SELECT * FROM favorite_pokemon ORDER BY pokemonId ASC")
    fun getAll(): Flow<List<FavoritePokemonEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_pokemon WHERE pokemonId = :pokemonId)")
    fun isFavorite(pokemonId: Int): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoritePokemonEntity)

    @Query("DELETE FROM favorite_pokemon WHERE pokemonId = :pokemonId")
    suspend fun delete(pokemonId: Int)
}

