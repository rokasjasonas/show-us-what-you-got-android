package com.rokas.showuswhatyougot

import androidx.annotation.StringRes
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rokas.showuswhatyougot.feature.details.PokemonDetailScreen
import com.rokas.showuswhatyougot.feature.details.PokemonDetailUiState
import com.rokas.showuswhatyougot.feature.favorites.FavoritesScreen
import com.rokas.showuswhatyougot.feature.list.PokemonListScreen
import com.rokas.showuswhatyougot.feature.list.PokemonUiState
import com.rokas.showuswhatyougot.model.Pokemon
import com.rokas.showuswhatyougot.ui.NoNetworkBanner

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavigation(
    currentDestination: AppDestinations,
    onDestinationChanged: (AppDestinations) -> Unit,
    isNetworkAvailable: Boolean,
    homeContent: HomeContent,
    favoritesContent: FavoritesContent,
    profileContent: ProfileContent,
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
                            targetState = homeContent.selectedPokemonId,
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
                                    uiState = homeContent.pokemonUiState,
                                    onRetry = homeContent.onRetryPokemonLoad,
                                    onLoadMore = homeContent.onLoadMorePokemon,
                                    onPokemonClick = homeContent.onPokemonSelected,
                                    onFavoriteToggle = homeContent.onFavoriteToggle,
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
                                    uiState = homeContent.pokemonDetailUiState,
                                    onBack = homeContent.onBackFromPokemonDetail,
                                    onRetry = homeContent.onRetryPokemonDetailLoad,
                                    onFavoriteToggle = homeContent.onDetailFavoriteToggle,
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

                AppDestinations.FAVORITES -> {
                    SharedTransitionLayout {
                        AnimatedContent(
                            targetState = favoritesContent.selectedFavoritePokemonId,
                            transitionSpec = {
                                if (targetState != null) {
                                    (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it / 3 } + fadeOut())
                                } else {
                                    (slideInHorizontally { -it / 3 } + fadeIn()) togetherWith (slideOutHorizontally { it } + fadeOut())
                                }
                            },
                            label = "FavoritesDetailTransition",
                        ) { pokemonId ->
                            if (pokemonId == null) {
                                FavoritesScreen(
                                    favorites = favoritesContent.favoritePokemon,
                                    onPokemonClick = favoritesContent.onFavoritePokemonSelected,
                                    onFavoriteToggle = favoritesContent.onFavoritePokemonToggle,
                                    modifier = Modifier.padding(innerPadding),
                                )
                            } else {
                                PokemonDetailScreen(
                                    uiState = favoritesContent.pokemonDetailUiState,
                                    onBack = favoritesContent.onBackFromFavoriteDetail,
                                    onRetry = favoritesContent.onRetryPokemonDetailLoad,
                                    onFavoriteToggle = favoritesContent.onDetailFavoriteToggle,
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

                AppDestinations.PROFILE -> {
                    com.rokas.showuswhatyougot.feature.profile.ProfileScreen(
                        onLanguageSelected = profileContent.onLanguageSelected,
                        isDarkMode = profileContent.isDarkMode,
                        onDarkModeToggled = profileContent.onDarkModeToggled,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}

data class HomeContent(
    val pokemonUiState: PokemonUiState,
    val selectedPokemonId: Int?,
    val pokemonDetailUiState: PokemonDetailUiState,
    val onLoadMorePokemon: () -> Unit,
    val onFavoriteToggle: (Int) -> Unit,
    val onPokemonSelected: (Int) -> Unit,
    val onBackFromPokemonDetail: () -> Unit,
    val onRetryPokemonDetailLoad: () -> Unit,
    val onRetryPokemonLoad: () -> Unit,
    val onDetailFavoriteToggle: () -> Unit,
)

data class FavoritesContent(
    val favoritePokemon: List<Pokemon>,
    val selectedFavoritePokemonId: Int?,
    val pokemonDetailUiState: PokemonDetailUiState,
    val onFavoritePokemonToggle: (Int) -> Unit,
    val onFavoritePokemonSelected: (Int) -> Unit,
    val onBackFromFavoriteDetail: () -> Unit,
    val onRetryPokemonDetailLoad: () -> Unit,
    val onDetailFavoriteToggle: () -> Unit,
)

data class ProfileContent(
    val onLanguageSelected: (String) -> Unit,
    val isDarkMode: Boolean,
    val onDarkModeToggled: (Boolean) -> Unit,
)

enum class AppDestinations(
    @param:StringRes val labelRes: Int,
    val icon: Int,
) {
    HOME(R.string.nav_home, R.drawable.ic_home),
    FAVORITES(R.string.nav_favorites, R.drawable.ic_favorite),
    PROFILE(R.string.nav_profile, R.drawable.ic_account_box),
}

