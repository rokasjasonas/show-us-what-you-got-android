package com.rokas.showuswhatyougot.debug.menu

import androidx.compose.runtime.mutableStateListOf
import com.rokas.showuswhatyougot.analytics.AnalyticsEvent
import com.rokas.showuswhatyougot.analytics.AnalyticsProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DebugAnalyticsProvider @Inject constructor() : AnalyticsProvider {

    private val _events = mutableStateListOf<DebugAnalyticsEntry>()
    val events: List<DebugAnalyticsEntry> get() = _events

    override fun trackEvent(event: AnalyticsEvent) {
        _events.add(
            DebugAnalyticsEntry(
                name = event.name,
                timestamp = System.currentTimeMillis(),
            )
        )
    }

    fun clear() {
        _events.clear()
    }
}

data class DebugAnalyticsEntry(
    val name: String,
    val timestamp: Long,
)

