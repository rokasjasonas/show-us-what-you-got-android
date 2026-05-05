package com.rokas.showuswhatyougot.analytics

sealed class AnalyticsEvent(val name: String, val params: Map<String, Any> = emptyMap()) {
    data object HomeScreenOpen : AnalyticsEvent("home_screen_open")
    data class DetailsScreenOpen(val pokemonId: Int) :
        AnalyticsEvent("details_screen_open", params = mapOf("pokemon_id" to pokemonId))

    data class PokemonClick(val pokemonId: Int) : AnalyticsEvent("pokemon_click")
    data object TryAgainClick : AnalyticsEvent("try_again_click")
    data object PullToRefresh : AnalyticsEvent("pull_to_refresh")
}

