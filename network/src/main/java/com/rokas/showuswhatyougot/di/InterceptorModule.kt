package com.rokas.showuswhatyougot.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds
import okhttp3.Interceptor

@Module
@InstallIn(SingletonComponent::class)
abstract class InterceptorModule {
    @Multibinds
    abstract fun interceptors(): Set<Interceptor>
}

