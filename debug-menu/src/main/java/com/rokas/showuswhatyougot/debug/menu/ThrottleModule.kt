package com.rokas.showuswhatyougot.debug.menu

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import okhttp3.Interceptor

@Module
@InstallIn(SingletonComponent::class)
object ThrottleModule {
    @Provides
    @IntoSet
    fun provideThrottleInterceptor(interceptor: ThrottleInterceptor): Interceptor = interceptor
}

