package com.rokas.showuswhatyougot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.rokas.showuswhatyougot.data.PokemonRepository
import com.rokas.showuswhatyougot.model.Pokemon
import com.rokas.showuswhatyougot.ui.pokemon.PokemonListScreen
import com.rokas.showuswhatyougot.ui.pokemon.PokemonUiState
import com.rokas.showuswhatyougot.ui.theme.ShowUsWhatYouGotTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShowUsWhatYouGotTheme {
                ShowUsWhatYouGotApp()
            }
        }
    }
}

@Composable
fun ShowUsWhatYouGotApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    var reloadKey by rememberSaveable { mutableIntStateOf(0) }

    val pokemonUiState by produceState<PokemonUiState>(
        initialValue = PokemonUiState.Loading,
        key1 = reloadKey,
    ) {
        value = PokemonUiState.Loading
        value = try {
            PokemonUiState.Success(PokemonRepository.getPokemon())
        } catch (exception: Exception) {
            PokemonUiState.Error(exception.message ?: "Please check your connection and try again.")
        }
    }

    ShowUsWhatYouGotAppContent(
        currentDestination = currentDestination,
        onDestinationChanged = { currentDestination = it },
        pokemonUiState = pokemonUiState,
        onRetryPokemonLoad = { reloadKey++ },
    )
}

@Composable
private fun ShowUsWhatYouGotAppContent(
    currentDestination: AppDestinations,
    onDestinationChanged: (AppDestinations) -> Unit,
    pokemonUiState: PokemonUiState,
    onRetryPokemonLoad: () -> Unit,
) {
    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            painter = painterResource(it.icon),
                            contentDescription = it.label,
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { onDestinationChanged(it) },
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            when (currentDestination) {
                AppDestinations.HOME -> PokemonListScreen(
                    uiState = pokemonUiState,
                    onRetry = onRetryPokemonLoad,
                    modifier = Modifier.padding(innerPadding),
                )

                AppDestinations.FAVORITES -> PlaceholderScreen(
                    title = "Favorites",
                    message = "Save your favorite Pokemon here next.",
                    modifier = Modifier.padding(innerPadding),
                )

                AppDestinations.PROFILE -> PlaceholderScreen(
                    title = "Profile",
                    message = "Trainer profile features can live here.",
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: Int,
) {
    HOME("Home", R.drawable.ic_home),
    FAVORITES("Favorites", R.drawable.ic_favorite),
    PROFILE("Profile", R.drawable.ic_account_box),
}

@Composable
private fun PlaceholderScreen(
    title: String,
    message: String,
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
                text = title,
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = message,
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
            pokemonUiState = PokemonUiState.Success(
                listOf(
                    Pokemon(25, "Pikachu", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/25.png"),
                    Pokemon(39, "Jigglypuff", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/39.png"),
                )
            ),
            onRetryPokemonLoad = {},
        )
    }
}