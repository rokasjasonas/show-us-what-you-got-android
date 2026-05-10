# show-us-what-you-got-android

* Screens
  * List - Pokédex list from PokeAPI with images and pagination
  * Details - Pokémon details (stats, types, abilities) with shared element transition
  * Profile - User profile with language settings (English/Lithuanian) and dark mode toggle
* Modules
  * [analytics](analytics) - Analytics engine with pluggable providers
  * [app](app) - Main app module, ViewModels, DI setup
  * [common](common) - Shared UI components, theme, models
  * [debug-menu](debug-menu) - Debug drawer with app info and analytics monitor
  * [feature-details](feature-details) - Pokémon detail screen
  * [feature-favorites](feature-favorites) - Favorites screen and ViewModel
  * [feature-list](feature-list) - Pokémon list screen
  * [feature-profile](feature-profile) - Profile & language settings screen
  * [network](network) - Retrofit API service, DTOs, Hilt network module
  * [storage](storage) - DataStore preferences manager, Room database caching
* Architecture
  * MVVM with Hilt DI
  * Multi-module
  * Version catalog (libs.versions.toml)
* Features
  * Pagination with auto-load on scroll
  * Favorites — heart icon on list items and detail screen, persisted in Room, dedicated Favorites tab
  * Offline-first caching with Room (shows cached data instantly, then updates from network)
  * No network banner with auto-retry on reconnect
  * Language switching (EN/LT) persisted to DataStore
  * Dark mode toggle persisted to DataStore (falls back to system theme)
* Debug mode
  * Debug drawer (swipe from left edge)
  * HTTP monitoring (Chucker)
  * Real-time analytics event log
  * Network throttle (3-second delay toggle)
* Analytics engine
  * Supports multiple providers simultaneously
  * Stub provider (Logcat) + Debug provider (in-app log)
* Unit tests
* Deploy
  * `./scripts/deploy.sh` — builds signed release AAB and uploads to Play Store internal testing
  * Uses Google Play Publishing API via Python (no Fastlane)
  * Setup:
    1. Create a Google Cloud service account with Play Store access
    2. Download the JSON key → save as `play-store-key.json` in project root
    3. Fill in `keystore.properties` with your signing credentials
    4. `pip install google-api-python-client oauth2client`
    5. `./scripts/deploy.sh`

TODO
* Showkase
* Design
