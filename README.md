# show-us-what-you-got-android

* Screens
  * List - Pokédex list from PokeAPI with images and pagination
  * Details - Pokémon details (stats, types, abilities) with shared element transition
  * Profile - User profile with language settings (English/Lithuanian)
* Modules
  * [analytics](analytics) - Analytics engine with pluggable providers
  * [app](app) - Main app module, ViewModels, DI setup
  * [common](common) - Shared UI components, theme, models
  * [debug-menu](debug-menu) - Debug drawer with app info and analytics monitor
  * [feature-details](feature-details) - Pokémon detail screen
  * [feature-list](feature-list) - Pokémon list screen
  * [feature-profile](feature-profile) - Profile & language settings screen
  * [network](network) - Retrofit API service, DTOs, Hilt network module
  * [storage](storage) - DataStore preferences manager
* Architecture
  * MVVM with Hilt DI
  * Multi-module
  * Version catalog (libs.versions.toml)
* Features
  * Pagination with auto-load on scroll
  * No network banner with auto-retry on reconnect
  * Language switching (EN/LT) persisted to DataStore
* Debug mode
  * Debug drawer (swipe from left edge)
  * HTTP monitoring (Chucker)
  * Real-time analytics event log
* Analytics engine
  * Supports multiple providers simultaneously
  * Stub provider (Logcat) + Debug provider (in-app log)
* Unit tests

TODO
* Showkase
* Caching
* Network throttle
* Design
* Dark mode
* CICD