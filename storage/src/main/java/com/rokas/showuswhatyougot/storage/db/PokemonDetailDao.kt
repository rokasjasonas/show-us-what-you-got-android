package com.rokas.showuswhatyougot.storage.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PokemonDetailDao {
    @Query("SELECT * FROM pokemon_detail WHERE id = :id")
    suspend fun getById(id: Int): PokemonDetailEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(detail: PokemonDetailEntity)
}

