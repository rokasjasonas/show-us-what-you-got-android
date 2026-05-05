package com.rokas.showuswhatyougot.analytics

import android.util.Log
import javax.inject.Inject

class StubAnalyticsProvider @Inject constructor() : AnalyticsProvider {
    override fun trackEvent(event: AnalyticsEvent) {
        Log.d("StubAnalytics", "Event: ${event.name}")
    }
}

