package com.rokas.showuswhatyougot.ui.pokemon

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.rokas.showuswhatyougot.model.Pokemon
import com.rokas.showuswhatyougot.ui.theme.ShowUsWhatYouGotTheme

sealed interface PokemonUiState {
    data object Loading : PokemonUiState
    data class Success(val pokemon: List<Pokemon>) : PokemonUiState
    data class Error(val message: String) : PokemonUiState
}

@Composable
fun PokemonListScreen(
    uiState: PokemonUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        PokemonUiState.Loading -> PokemonLoadingState(modifier = modifier)
        is PokemonUiState.Error -> PokemonErrorState(
            message = uiState.message,
            onRetry = onRetry,
            modifier = modifier,
        )
        is PokemonUiState.Success -> PokemonContent(
            pokemon = uiState.pokemon,
            modifier = modifier,
        )
    }
}

@Composable
private fun PokemonLoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun PokemonErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Could not load Pokémon",
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(onClick = onRetry) {
                Text(text = "Try again")
            }
        }
    }
}

@Composable
private fun PokemonContent(
    pokemon: List<Pokemon>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Pokédex",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "First ${pokemon.size} Pokémon from PokeAPI",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        items(
            items = pokemon,
            key = Pokemon::id,
        ) { item ->
            PokemonRow(
                pokemon = item,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        item {
            Box(modifier = Modifier.size(4.dp))
        }
    }
}

@Composable
private fun PokemonRow(
    pokemon: Pokemon,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AsyncImage(
                model = pokemon.imageUrl,
                contentDescription = pokemon.name,
                modifier = Modifier.size(88.dp),
                contentScale = ContentScale.Fit,
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = pokemon.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "#${pokemon.id.toString().padStart(3, '0')}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PokemonListScreenPreview() {
    ShowUsWhatYouGotTheme {
        PokemonListScreen(
            uiState = PokemonUiState.Success(
                listOf(
                    Pokemon(1, "Bulbasaur", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png"),
                    Pokemon(4, "Charmander", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/4.png"),
                    Pokemon(7, "Squirtle", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/7.png"),
                )
            ),
            onRetry = {},
        )
    }
}

