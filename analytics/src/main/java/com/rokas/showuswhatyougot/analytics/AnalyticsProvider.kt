package com.rokas.showuswhatyougot.analytics

interface AnalyticsProvider {
    fun trackEvent(event: AnalyticsEvent)
}

