package com.rokas.showuswhatyougot.storage.db

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PokemonDatabase {
        return Room.databaseBuilder(
            context,
            PokemonDatabase::class.java,
            "pokemon_db"
        ).fallbackToDestructiveMigration(dropAllTables = true).build()
    }

    @Provides
    fun providePokemonDao(database: PokemonDatabase): PokemonDao = database.pokemonDao()

    @Provides
    fun providePokemonDetailDao(database: PokemonDatabase): PokemonDetailDao = database.pokemonDetailDao()

    @Provides
    fun provideFavoritePokemonDao(database: PokemonDatabase): FavoritePokemonDao = database.favoritePokemonDao()
}

