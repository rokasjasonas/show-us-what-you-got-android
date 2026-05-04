package com.rokas.showuswhatyougot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
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
import com.rokas.showuswhatyougot.data.PokemonRepository
import com.rokas.showuswhatyougot.model.Pokemon
import com.rokas.showuswhatyougot.ui.pokemon.PokemonDetailScreen
import com.rokas.showuswhatyougot.ui.pokemon.PokemonDetailUiState
import com.rokas.showuswhatyougot.ui.pokemon.PokemonListScreen
import com.rokas.showuswhatyougot.ui.pokemon.PokemonUiState
import com.rokas.showuswhatyougot.ui.theme.ShowUsWhatYouGotTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var pokemonRepository: PokemonRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShowUsWhatYouGotTheme {
                ShowUsWhatYouGotApp(
                    pokemonRepository = pokemonRepository,
                )
            }
        }
    }
}

private const val POKEMON_PAGE_SIZE = 30

@Composable
fun ShowUsWhatYouGotApp(
    pokemonRepository: PokemonRepository,
) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    var reloadKey by rememberSaveable { mutableIntStateOf(0) }
    var selectedPokemonId by rememberSaveable { mutableStateOf<Int?>(null) }
    var detailReloadKey by rememberSaveable { mutableIntStateOf(0) }
    var pokemonUiState by remember {
        mutableStateOf(
            PokemonUiState(
                isInitialLoading = true,
                nextOffset = 0,
            )
        )
    }
    val scope = rememberCoroutineScope()

    suspend fun loadInitialPokemonPage() {
        pokemonUiState = PokemonUiState(
            isInitialLoading = true,
            nextOffset = 0,
        )

        pokemonUiState = try {
            val page = pokemonRepository.getPokemonPage(
                limit = POKEMON_PAGE_SIZE,
                offset = 0,
            )
            PokemonUiState(
                pokemon = page.pokemon,
                nextOffset = page.nextOffset,
            )
        } catch (exception: Exception) {
            PokemonUiState(
                initialErrorMessage = exception.message.orEmpty(),
                nextOffset = 0,
            )
        }
    }

    fun loadNextPokemonPage() {
        val currentState = pokemonUiState
        val nextOffset = currentState.nextOffset ?: return

        if (currentState.isInitialLoading || currentState.isAppending) {
            return
        }

        pokemonUiState = currentState.copy(
            isAppending = true,
            appendErrorMessage = "",
        )

        scope.launch {
            pokemonUiState = try {
                val page = pokemonRepository.getPokemonPage(
                    limit = POKEMON_PAGE_SIZE,
                    offset = nextOffset,
                )
                currentState.copy(
                    pokemon = currentState.pokemon + page.pokemon,
                    isAppending = false,
                    appendErrorMessage = "",
                    nextOffset = page.nextOffset,
                )
            } catch (exception: Exception) {
                currentState.copy(
                    isAppending = false,
                    appendErrorMessage = exception.message.orEmpty(),
                )
            }
        }
    }

    LaunchedEffect(reloadKey) {
        loadInitialPokemonPage()
    }

    val selectedPokemonDetailState by produceState<SelectedPokemonDetailState?>(
        initialValue = null,
        key1 = selectedPokemonId,
        key2 = detailReloadKey,
    ) {
        val pokemonId = selectedPokemonId ?: return@produceState
        value = SelectedPokemonDetailState(
            pokemonId = pokemonId,
            uiState = PokemonDetailUiState.Loading,
        )
        value = try {
            SelectedPokemonDetailState(
                pokemonId = pokemonId,
                uiState = PokemonDetailUiState.Success(pokemonRepository.getPokemonDetail(pokemonId)),
            )
        } catch (exception: Exception) {
            SelectedPokemonDetailState(
                pokemonId = pokemonId,
                uiState = PokemonDetailUiState.Error(exception.message.orEmpty()),
            )
        }
    }

    val pokemonDetailUiState =
        if (selectedPokemonDetailState?.pokemonId == selectedPokemonId) {
            selectedPokemonDetailState?.uiState ?: PokemonDetailUiState.Loading
        } else {
            PokemonDetailUiState.Loading
        }

    BackHandler(enabled = selectedPokemonId != null) {
        selectedPokemonId = null
    }

    ShowUsWhatYouGotAppContent(
        currentDestination = currentDestination,
        onDestinationChanged = { currentDestination = it },
        pokemonUiState = pokemonUiState,
        onLoadMorePokemon = ::loadNextPokemonPage,
        selectedPokemonId = selectedPokemonId,
        pokemonDetailUiState = pokemonDetailUiState,
        onPokemonSelected = {
            selectedPokemonId = it
            detailReloadKey = 0
        },
        onBackFromPokemonDetail = { selectedPokemonId = null },
        onRetryPokemonDetailLoad = { detailReloadKey++ },
        onRetryPokemonLoad = { reloadKey++ },
    )
}

private data class SelectedPokemonDetailState(
    val pokemonId: Int,
    val uiState: PokemonDetailUiState,
)

@Composable
private fun ShowUsWhatYouGotAppContent(
    currentDestination: AppDestinations,
    onDestinationChanged: (AppDestinations) -> Unit,
    pokemonUiState: PokemonUiState,
    onLoadMorePokemon: () -> Unit,
    selectedPokemonId: Int?,
    pokemonDetailUiState: PokemonDetailUiState,
    onPokemonSelected: (Int) -> Unit,
    onBackFromPokemonDetail: () -> Unit,
    onRetryPokemonDetailLoad: () -> Unit,
    onRetryPokemonLoad: () -> Unit,
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
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            when (currentDestination) {
                AppDestinations.HOME -> if (selectedPokemonId == null) {
                    PokemonListScreen(
                        uiState = pokemonUiState,
                        onRetry = onRetryPokemonLoad,
                        onLoadMore = onLoadMorePokemon,
                        onPokemonClick = onPokemonSelected,
                        modifier = Modifier.padding(innerPadding),
                    )
                } else {
                    PokemonDetailScreen(
                        uiState = pokemonDetailUiState,
                        onBack = onBackFromPokemonDetail,
                        onRetry = onRetryPokemonDetailLoad,
                        modifier = Modifier.padding(innerPadding),
                    )
                }

                AppDestinations.FAVORITES -> PlaceholderScreen(
                    titleRes = R.string.nav_favorites,
                    messageRes = R.string.favorites_placeholder_message,
                    modifier = Modifier.padding(innerPadding),
                )

                AppDestinations.PROFILE -> PlaceholderScreen(
                    titleRes = R.string.nav_profile,
                    messageRes = R.string.profile_placeholder_message,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

enum class AppDestinations(
    @param:StringRes val labelRes: Int,
    val icon: Int,
) {
    HOME(R.string.nav_home, R.drawable.ic_home),
    FAVORITES(R.string.nav_favorites, R.drawable.ic_favorite),
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
            pokemonUiState = PokemonUiState(
                pokemon = listOf(
                    Pokemon(25, "Pikachu", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/25.png"),
                    Pokemon(39, "Jigglypuff", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/39.png"),
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
        )
    }
}