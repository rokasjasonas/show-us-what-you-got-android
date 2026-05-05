package com.rokas.showuswhatyougot.analytics.di

import com.rokas.showuswhatyougot.analytics.AnalyticsProvider
import com.rokas.showuswhatyougot.analytics.StubAnalyticsProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {

    @Binds
    @IntoSet
    abstract fun bindStubAnalyticsProvider(impl: StubAnalyticsProvider): AnalyticsProvider
}

