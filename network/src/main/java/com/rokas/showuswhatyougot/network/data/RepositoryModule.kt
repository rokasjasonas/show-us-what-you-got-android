package com.rokas.showuswhatyougot.network.data

import com.rokas.showuswhatyougot.data.PokemonRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindPokemonRepository(impl: PokemonRepositoryImpl): PokemonRepository
}

