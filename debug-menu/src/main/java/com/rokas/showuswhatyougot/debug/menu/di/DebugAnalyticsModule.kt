package com.rokas.showuswhatyougot.debug.menu.di

import com.rokas.showuswhatyougot.analytics.AnalyticsProvider
import com.rokas.showuswhatyougot.debug.menu.DebugAnalyticsProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class DebugAnalyticsModule {

    @Binds
    @IntoSet
    abstract fun bindDebugAnalyticsProvider(impl: DebugAnalyticsProvider): AnalyticsProvider
}

