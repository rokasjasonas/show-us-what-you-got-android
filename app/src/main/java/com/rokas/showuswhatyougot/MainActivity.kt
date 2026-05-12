package com.rokas.showuswhatyougot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.os.LocaleListCompat
import com.rokas.showuswhatyougot.feature.details.PokemonDetailViewModel
import com.rokas.showuswhatyougot.feature.favorites.FavoritesViewModel
import com.rokas.showuswhatyougot.feature.list.PokemonListViewModel
import com.rokas.showuswhatyougot.storage.PreferencesManager
import com.rokas.showuswhatyougot.ui.theme.ShowUsWhatYouGotTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restoreSavedLocale()
        enableEdgeToEdge()
        setContent {
            val storedDarkMode by preferencesManager.darkModeEnabled.collectAsState(initial = null)
            val systemDark = isSystemInDarkTheme()
            val isDark = storedDarkMode ?: systemDark

            LaunchedEffect(isDark) {
                enableEdgeToEdge(
                    statusBarStyle = if (isDark) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        )
                    },
                    navigationBarStyle = if (isDark) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        )
                    },
                )
            }

            ShowUsWhatYouGotTheme(darkTheme = isDark) {
                DebugDrawerWrapper {
                    ShowUsWhatYouGotApp(preferencesManager = preferencesManager)
                }
            }
        }
    }

    private fun restoreSavedLocale() {
        val currentLocales = AppCompatDelegate.getApplicationLocales()
        if (!currentLocales.isEmpty) return

        runBlocking {
            preferencesManager.selectedLanguage.firstOrNull()?.let { tag ->
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(tag)
                )
            }
        }
    }
}

@Composable
fun ShowUsWhatYouGotApp(
    preferencesManager: PreferencesManager,
) {
    val listViewModel: PokemonListViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val homeDetailViewModel: PokemonDetailViewModel = androidx.hilt.navigation.compose.hiltViewModel(key = "home_detail")
    val favDetailViewModel: PokemonDetailViewModel = androidx.hilt.navigation.compose.hiltViewModel(key = "fav_detail")
    val favoritesViewModel: FavoritesViewModel = androidx.hilt.navigation.compose.hiltViewModel()

    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    val systemDark = isSystemInDarkTheme()
    val storedDarkMode by preferencesManager.darkModeEnabled.collectAsState(initial = null)
    val isDarkMode = storedDarkMode ?: systemDark
    var selectedPokemonId by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedFavoritePokemonId by rememberSaveable { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()

    val pokemonUiState by listViewModel.uiState.collectAsState()
    val homeDetailUiState by homeDetailViewModel.uiState.collectAsState()
    val favDetailUiState by favDetailViewModel.uiState.collectAsState()
    val favoritePokemon by favoritesViewModel.favorites.collectAsState()

    val isNetworkAvailable = rememberNetworkAvailability()

    LaunchedEffect(isNetworkAvailable) {
        if (isNetworkAvailable) {
            listViewModel.onNetworkRestored()
            homeDetailViewModel.onNetworkRestored()
            favDetailViewModel.onNetworkRestored()
        }
    }

    LaunchedEffect(selectedPokemonId) {
        selectedPokemonId?.let { homeDetailViewModel.loadPokemon(it) }
    }

    LaunchedEffect(selectedFavoritePokemonId) {
        selectedFavoritePokemonId?.let { favDetailViewModel.loadPokemon(it) }
    }

    LaunchedEffect(currentDestination) {
        if (currentDestination == AppDestinations.HOME) {
            listViewModel.onScreenOpened()
        }
    }

    BackHandler(enabled = selectedPokemonId != null) {
        selectedPokemonId = null
    }

    BackHandler(enabled = selectedFavoritePokemonId != null) {
        selectedFavoritePokemonId = null
    }

    AppNavigation(
        currentDestination = currentDestination,
        onDestinationChanged = { dest ->
            if (dest == currentDestination) {
                when (dest) {
                    AppDestinations.HOME -> selectedPokemonId = null
                    AppDestinations.FAVORITES -> selectedFavoritePokemonId = null
                    else -> {}
                }
            } else {
                currentDestination = dest
            }
        },
        isNetworkAvailable = isNetworkAvailable,
        homeContent = HomeContent(
            pokemonUiState = pokemonUiState,
            selectedPokemonId = selectedPokemonId,
            pokemonDetailUiState = homeDetailUiState,
            onLoadMorePokemon = { listViewModel.loadNextPage() },
            onFavoriteToggle = { listViewModel.toggleFavorite(it) },
            onPokemonSelected = {
                listViewModel.onPokemonClick(it)
                selectedPokemonId = it
            },
            onBackFromPokemonDetail = { selectedPokemonId = null },
            onRetryPokemonDetailLoad = { homeDetailViewModel.retry() },
            onRetryPokemonLoad = { listViewModel.retry() },
            onDetailFavoriteToggle = { homeDetailViewModel.toggleFavorite() },
        ),
        favoritesContent = FavoritesContent(
            favoritePokemon = favoritePokemon,
            selectedFavoritePokemonId = selectedFavoritePokemonId,
            pokemonDetailUiState = favDetailUiState,
            onFavoritePokemonToggle = { favoritesViewModel.toggleFavorite(it) },
            onFavoritePokemonSelected = { selectedFavoritePokemonId = it },
            onBackFromFavoriteDetail = { selectedFavoritePokemonId = null },
            onRetryPokemonDetailLoad = { favDetailViewModel.retry() },
            onDetailFavoriteToggle = { favDetailViewModel.toggleFavorite() },
        ),
        profileContent = ProfileContent(
            onLanguageSelected = { tag ->
                scope.launch { preferencesManager.setSelectedLanguage(tag) }
            },
            isDarkMode = isDarkMode,
            onDarkModeToggled = { enabled ->
                scope.launch { preferencesManager.setDarkModeEnabled(enabled) }
            },
        ),
    )
}