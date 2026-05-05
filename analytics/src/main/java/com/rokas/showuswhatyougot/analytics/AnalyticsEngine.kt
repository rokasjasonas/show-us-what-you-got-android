package com.rokas.showuswhatyougot.analytics

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsEngine @Inject constructor(
    private val providers: Set<@JvmSuppressWildcards AnalyticsProvider>,
) {
    fun trackEvent(event: AnalyticsEvent) {
        providers.forEach { it.trackEvent(event) }
    }
}

