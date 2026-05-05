package com.rokas.showuswhatyougot.analytics

import org.junit.Assert.assertEquals
import org.junit.Test

class AnalyticsEngineTest {

    @Test
    fun `trackEvent dispatches to all providers`() {
        val recorded = mutableListOf<AnalyticsEvent>()
        val provider1 = object : AnalyticsProvider {
            override fun trackEvent(event: AnalyticsEvent) { recorded.add(event) }
        }
        val provider2 = object : AnalyticsProvider {
            override fun trackEvent(event: AnalyticsEvent) { recorded.add(event) }
        }
        val engine = AnalyticsEngine(setOf(provider1, provider2))

        engine.trackEvent(AnalyticsEvent.HomeScreenOpen)

        assertEquals(2, recorded.size)
        assertEquals(AnalyticsEvent.HomeScreenOpen, recorded[0])
        assertEquals(AnalyticsEvent.HomeScreenOpen, recorded[1])
    }

    @Test
    fun `trackEvent works with empty providers`() {
        val engine = AnalyticsEngine(emptySet())

        // Should not throw
        engine.trackEvent(AnalyticsEvent.PokemonClick(1))
    }

    @Test
    fun `events have correct names`() {
        assertEquals("home_screen_open", AnalyticsEvent.HomeScreenOpen.name)
        assertEquals("details_screen_open", AnalyticsEvent.DetailsScreenOpen(1).name)
        assertEquals("pokemon_click", AnalyticsEvent.PokemonClick(25).name)
        assertEquals("try_again_click", AnalyticsEvent.TryAgainClick.name)
        assertEquals("pull_to_refresh", AnalyticsEvent.PullToRefresh.name)
    }
}

