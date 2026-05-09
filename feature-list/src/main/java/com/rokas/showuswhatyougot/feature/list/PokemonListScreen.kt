package com.rokas.showuswhatyougot.feature.list

import com.rokas.showuswhatyougot.feature.list.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.rokas.showuswhatyougot.model.Pokemon
import com.rokas.showuswhatyougot.ui.theme.ShowUsWhatYouGotTheme

data class PokemonUiState(
    val pokemon: List<Pokemon> = emptyList(),
    val favoriteIds: Set<Int> = emptySet(),
    val isInitialLoading: Boolean = false,
    val isAppending: Boolean = false,
    val initialErrorMessage: String = "",
    val appendErrorMessage: String = "",
    val nextOffset: Int? = 0,
)

@Composable
fun PokemonListScreen(
    uiState: PokemonUiState,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onPokemonClick: (Int) -> Unit,
    onFavoriteToggle: (Int) -> Unit,
    modifier: Modifier = Modifier,
    imageModifier: @Composable (pokemonId: Int) -> Modifier = { Modifier },
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
            imageModifier = imageModifier,
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
    val errorMessage = message.ifBlank { stringResource(R.string.pokemon_error_generic) }

    Box(
        modifier = modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
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
    imageModifier: @Composable (pokemonId: Int) -> Modifier,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val shouldLoadMore by remember(pokemon, canLoadMore, isAppending) {
        derivedStateOf {
            if (!canLoadMore || isAppending || pokemon.isEmpty()) {
                false
            } else {
                val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
                lastVisibleItemIndex >= pokemon.lastIndex - 4
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
                    text = stringResource(R.string.pokedex_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.pokemon_list_subtitle, pokemon.size),
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
                isFavorite = item.id in favoriteIds,
                onClick = { onPokemonClick(item.id) },
                onFavoriteToggle = { onFavoriteToggle(item.id) },
                imageModifier = imageModifier(item.id),
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        if (isAppending) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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
            verticalArrangement = Arrangement.spacedBy(8.dp),
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

@Composable
private fun PokemonRow(
    pokemon: Pokemon,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    imageModifier: Modifier = Modifier,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SubcomposeAsyncImage(
                model = pokemon.imageUrl,
                contentDescription = pokemon.name,
                modifier = imageModifier.size(88.dp),
                contentScale = ContentScale.Fit,
                loading = {
                    PokemonImagePlaceholder(isLoading = true)
                },
                error = {
                    PokemonImagePlaceholder(isLoading = false)
                },
            )

            Column(
                modifier = Modifier.weight(1f),
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

            IconButton(onClick = onFavoriteToggle) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PokemonImagePlaceholder(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(28.dp))
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_image_placeholder),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(36.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PokemonListScreenPreview() {
    ShowUsWhatYouGotTheme {
        PokemonListScreen(
            uiState = PokemonUiState(
                pokemon = listOf(
                    Pokemon(1, "Bulbasaur", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png"),
                    Pokemon(4, "Charmander", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/4.png"),
                    Pokemon(7, "Squirtle", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/7.png"),
                ),
                nextOffset = 20,
            ),
            onRetry = {},
            onLoadMore = {},
            onPokemonClick = {},
            onFavoriteToggle = {},
        )
    }
}

