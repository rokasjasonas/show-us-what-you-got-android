package com.rokas.showuswhatyougot.feature.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rokas.showuswhatyougot.model.Pokemon
import com.rokas.showuswhatyougot.ui.PokemonItemView

data class PokemonUiState(
    val pokemon: List<Pokemon> = emptyList(),
    val favoriteIds: Set<Int> = emptySet(),
    val isInitialLoading: Boolean = false,
    val isAppending: Boolean = false,
    val initialErrorMessage: String = "",
    val appendErrorMessage: String = "",
    val nextOffset: Int? = null,
)

@Composable
fun PokemonListScreen(
    uiState: PokemonUiState,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onPokemonClick: (Int) -> Unit,
    onFavoriteToggle: (Int) -> Unit,
    modifier: Modifier = Modifier,
    imageModifier: @Composable (Int) -> Modifier = { Modifier },
) {
    when {
        uiState.isInitialLoading && uiState.pokemon.isEmpty() -> PokemonLoadingState(modifier = modifier)

        uiState.initialErrorMessage.isNotBlank() && uiState.pokemon.isEmpty() -> PokemonErrorState(
            message = uiState.initialErrorMessage,
            onRetry = onRetry,
            modifier = modifier,
        )

        else -> PokemonContent(
            pokemon = uiState.pokemon,
            favoriteIds = uiState.favoriteIds,
            isAppending = uiState.isAppending,
            appendErrorMessage = uiState.appendErrorMessage,
            canLoadMore = uiState.nextOffset != null,
            onLoadMore = onLoadMore,
            onRetryAppend = onLoadMore,
            onPokemonClick = onPokemonClick,
            onFavoriteToggle = onFavoriteToggle,
            modifier = modifier,
            imageModifier = imageModifier,
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
    val errorMessage = message.ifBlank { stringResource(R.string.pokemon_error_generic) }

    Box(
        modifier = modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.pokemon_load_error_title),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(onClick = onRetry) {
                Text(text = stringResource(R.string.try_again))
            }
        }
    }
}

@Composable
private fun PokemonContent(
    pokemon: List<Pokemon>,
    favoriteIds: Set<Int>,
    isAppending: Boolean,
    appendErrorMessage: String,
    canLoadMore: Boolean,
    onLoadMore: () -> Unit,
    onRetryAppend: () -> Unit,
    onPokemonClick: (Int) -> Unit,
    onFavoriteToggle: (Int) -> Unit,
    modifier: Modifier = Modifier,
    imageModifier: @Composable (Int) -> Modifier = { Modifier },
) {
    val listState = rememberLazyListState()
    val shouldLoadMore by remember(pokemon, canLoadMore, isAppending) {
        derivedStateOf {
            if (!canLoadMore || isAppending || pokemon.isEmpty()) {
                false
            } else {
                val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
                // Load more when the user is near the bottom (e.g., 4 items away)
                lastVisibleItemIndex >= pokemon.size - 4
            }
        }
    }

    LaunchedEffect(shouldLoadMore, pokemon.size, isAppending, canLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(R.string.pokedex_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.pokemon_list_subtitle, pokemon.size),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        items(
            items = pokemon,
            key = { it.id }, // Using lambda for key extraction
        ) { item ->
            // Using the shared component directly with padding applied to the container of the shared view
            PokemonItemView(
                pokemon = item,
                isFavorite = item.id in favoriteIds,
                onClick = { onPokemonClick(item.id) },
                onFavoriteToggle = onFavoriteToggle,
                modifier = Modifier.padding(horizontal = 16.dp),
                imageModifier = imageModifier(item.id),
            )
        }

        if (isAppending) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        if (appendErrorMessage.isNotBlank()) {
            item {
                PokemonAppendError(
                    message = appendErrorMessage,
                    onRetry = onRetryAppend,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }

        // Footer spacer
        item {
            Box(modifier = Modifier.size(4.dp))
        }
    }
}

@Composable
private fun PokemonAppendError(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val errorMessage = message.ifBlank { stringResource(R.string.pokemon_error_generic) }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(onClick = onRetry) {
                Text(text = stringResource(R.string.try_again))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PokemonListScreenPreview() {
    // Placeholder preview setup, as resources are missing for full compile success.
    // The focus remains on structural correctness of the refactoring.
    MaterialTheme {
        PokemonListScreen(
            uiState = PokemonUiState(
                pokemon = listOf(
                    Pokemon(1, "Bulbasaur", ""),
                    Pokemon(4, "Charmander", "")
                ),
                nextOffset = null,
            ),
            onRetry = {},
            onLoadMore = {},
            onPokemonClick = {},
            onFavoriteToggle = {},
        )
    }
}