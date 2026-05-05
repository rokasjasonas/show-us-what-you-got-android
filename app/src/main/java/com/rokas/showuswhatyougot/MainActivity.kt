package com.rokas.showuswhatyougot

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.rokas.showuswhatyougot.feature.details.PokemonDetailScreen
import com.rokas.showuswhatyougot.feature.details.PokemonDetailUiState
import com.rokas.showuswhatyougot.feature.details.PokemonDetailViewModel
import com.rokas.showuswhatyougot.feature.list.PokemonListScreen
import com.rokas.showuswhatyougot.feature.list.PokemonListViewModel
import com.rokas.showuswhatyougot.feature.list.PokemonUiState
import com.rokas.showuswhatyougot.model.Pokemon
import com.rokas.showuswhatyougot.storage.PreferencesManager
import com.rokas.showuswhatyougot.ui.NoNetworkBanner
import com.rokas.showuswhatyougot.ui.theme.ShowUsWhatYouGotTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
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
            ShowUsWhatYouGotTheme {
                DebugDrawerWrapper {
                    ShowUsWhatYouGotApp(
                        preferencesManager = preferencesManager,
                    )
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
    val detailViewModel: PokemonDetailViewModel = androidx.hilt.navigation.compose.hiltViewModel()

    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    val currentLanguageTag by preferencesManager.selectedLanguage.collectAsState(initial = null)
    var selectedPokemonId by rememberSaveable { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()

    val pokemonUiState by listViewModel.uiState.collectAsState()
    val pokemonDetailUiState by detailViewModel.uiState.collectAsState()

    val isNetworkAvailable = rememberNetworkAvailability()

    LaunchedEffect(isNetworkAvailable) {
        if (isNetworkAvailable) {
            listViewModel.onNetworkRestored()
            detailViewModel.onNetworkRestored()
        }
    }

    LaunchedEffect(selectedPokemonId) {
        selectedPokemonId?.let { detailViewModel.loadPokemon(it) }
    }

    LaunchedEffect(currentDestination) {
        if (currentDestination == AppDestinations.HOME) {
            listViewModel.onScreenOpened()
        }
    }

    BackHandler(enabled = selectedPokemonId != null) {
        selectedPokemonId = null
    }

    ShowUsWhatYouGotAppContent(
        currentDestination = currentDestination,
        onDestinationChanged = { currentDestination = it },
        isNetworkAvailable = isNetworkAvailable,
        pokemonUiState = pokemonUiState,
        onLoadMorePokemon = { listViewModel.loadNextPage() },
        selectedPokemonId = selectedPokemonId,
        pokemonDetailUiState = pokemonDetailUiState,
        onPokemonSelected = {
            listViewModel.onPokemonClick(it)
            selectedPokemonId = it
        },
        onBackFromPokemonDetail = { selectedPokemonId = null },
        onRetryPokemonDetailLoad = { detailViewModel.retry() },
        onRetryPokemonLoad = { listViewModel.retry() },
        onLanguageSelected = { tag ->
            scope.launch { preferencesManager.setSelectedLanguage(tag) }
        },
        currentLanguageTag = currentLanguageTag,
    )
}


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ShowUsWhatYouGotAppContent(
    currentDestination: AppDestinations,
    onDestinationChanged: (AppDestinations) -> Unit,
    isNetworkAvailable: Boolean,
    pokemonUiState: PokemonUiState,
    onLoadMorePokemon: () -> Unit,
    selectedPokemonId: Int?,
    pokemonDetailUiState: PokemonDetailUiState,
    onPokemonSelected: (Int) -> Unit,
    onBackFromPokemonDetail: () -> Unit,
    onRetryPokemonDetailLoad: () -> Unit,
    onRetryPokemonLoad: () -> Unit,
    onLanguageSelected: (String) -> Unit,
    currentLanguageTag: String?,
) {
    val context = LocalContext.current

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                val label = context.getString(it.labelRes)
                item(
                    icon = {
                        Icon(
                            painter = painterResource(it.icon),
                            contentDescription = label,
                        )
                    },
                    label = { Text(label) },
                    selected = it == currentDestination,
                    onClick = { onDestinationChanged(it) },
                )
            }
        }) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (!isNetworkAvailable) {
                    NoNetworkBanner()
                }
            },
        ) { innerPadding ->
            when (currentDestination) {
                AppDestinations.HOME -> {
                    SharedTransitionLayout {
                        AnimatedContent(
                            targetState = selectedPokemonId,
                            transitionSpec = {
                                if (targetState != null) {
                                    (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it / 3 } + fadeOut())
                                } else {
                                    (slideInHorizontally { -it / 3 } + fadeIn()) togetherWith (slideOutHorizontally { it } + fadeOut())
                                }
                            },
                            label = "HomeDetailTransition",
                        ) { pokemonId ->
                            if (pokemonId == null) {
                                PokemonListScreen(
                                    uiState = pokemonUiState,
                                    onRetry = onRetryPokemonLoad,
                                    onLoadMore = onLoadMorePokemon,
                                    onPokemonClick = onPokemonSelected,
                                    modifier = Modifier.padding(innerPadding),
                                    imageModifier = { id ->
                                        Modifier.sharedElement(
                                            rememberSharedContentState(key = "pokemon_image_$id"),
                                            animatedVisibilityScope = this@AnimatedContent,
                                        )
                                    },
                                )
                            } else {
                                PokemonDetailScreen(
                                    uiState = pokemonDetailUiState,
                                    onBack = onBackFromPokemonDetail,
                                    onRetry = onRetryPokemonDetailLoad,
                                    modifier = Modifier.padding(innerPadding),
                                    imageModifier = Modifier.sharedElement(
                                        rememberSharedContentState(key = "pokemon_image_$pokemonId"),
                                        animatedVisibilityScope = this@AnimatedContent,
                                    ),
                                )
                            }
                        }
                    }
                }

                AppDestinations.FAVORITES -> PlaceholderScreen(
                    titleRes = R.string.nav_favorites,
                    messageRes = R.string.favorites_placeholder_message,
                    modifier = Modifier.padding(innerPadding),
                )

                AppDestinations.PROFILE -> com.rokas.showuswhatyougot.feature.profile.ProfileScreen(
                    onLanguageSelected = onLanguageSelected,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

@Composable
private fun rememberNetworkAvailability(): Boolean {
    val context = LocalContext.current.applicationContext
    val connectivityManager = remember(context) {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    val isNetworkAvailable by remember(connectivityManager) {
        callbackFlow {
            trySend(connectivityManager.isCurrentlyConnected())

            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    trySend(true)
                }

                override fun onLost(network: Network) {
                    trySend(connectivityManager.isCurrentlyConnected())
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities,
                ) {
                    trySend(networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
                }

                override fun onUnavailable() {
                    trySend(false)
                }
            }

            connectivityManager.registerDefaultNetworkCallback(callback)

            awaitClose {
                connectivityManager.unregisterNetworkCallback(callback)
            }
        }.conflate()
    }.collectAsState(initial = connectivityManager.isCurrentlyConnected())

    return isNetworkAvailable
}

private fun ConnectivityManager.isCurrentlyConnected(): Boolean {
    val activeNetwork = activeNetwork ?: return false
    val capabilities = getNetworkCapabilities(activeNetwork) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

enum class AppDestinations(
    @param:StringRes val labelRes: Int,
    val icon: Int,
) {
    HOME(R.string.nav_home, R.drawable.ic_home), FAVORITES(
        R.string.nav_favorites,
        R.drawable.ic_favorite
    ),
    PROFILE(R.string.nav_profile, R.drawable.ic_account_box),
}

@Composable
private fun PlaceholderScreen(
    @StringRes titleRes: Int,
    @StringRes messageRes: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = stringResource(messageRes),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@PreviewScreenSizes
@Preview(showBackground = true)
@Composable
private fun ShowUsWhatYouGotAppPreview() {
    ShowUsWhatYouGotTheme {
        ShowUsWhatYouGotAppContent(
            currentDestination = AppDestinations.HOME,
            onDestinationChanged = {},
            isNetworkAvailable = false,
            pokemonUiState = PokemonUiState(
                pokemon = listOf(
                    Pokemon(
                        25,
                        "Pikachu",
                        "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/25.png"
                    ),
                    Pokemon(
                        39,
                        "Jigglypuff",
                        "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/39.png"
                    ),
                ),
                nextOffset = 30,
            ),
            onLoadMorePokemon = {},
            selectedPokemonId = null,
            pokemonDetailUiState = PokemonDetailUiState.Loading,
            onPokemonSelected = {},
            onBackFromPokemonDetail = {},
            onRetryPokemonDetailLoad = {},
            onRetryPokemonLoad = {},
            onLanguageSelected = {},
            currentLanguageTag = null,
        )
    }
}