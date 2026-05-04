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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.annotation.StringRes
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
    val genericErrorMessage = stringResource(R.string.pokemon_error_generic)

    val pokemonUiState by produceState<PokemonUiState>(
        initialValue = PokemonUiState.Loading,
        key1 = reloadKey,
    ) {
        value = PokemonUiState.Loading
        value = try {
            PokemonUiState.Success(PokemonRepository.getPokemon())
        } catch (exception: Exception) {
            PokemonUiState.Error(exception.message ?: genericErrorMessage)
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
                AppDestinations.HOME -> PokemonListScreen(
                    uiState = pokemonUiState,
                    onRetry = onRetryPokemonLoad,
                    modifier = Modifier.padding(innerPadding),
                )

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