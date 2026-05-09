package com.rokas.showuswhatyougot.feature.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.rokas.showuswhatyougot.feature.details.R
import com.rokas.showuswhatyougot.model.PokemonDetail
import com.rokas.showuswhatyougot.ui.theme.ShowUsWhatYouGotTheme

sealed interface PokemonDetailUiState {
    data object Loading : PokemonDetailUiState
    data class Success(val pokemon: PokemonDetail, val isFavorite: Boolean = false) : PokemonDetailUiState
    data class Error(val message: String) : PokemonDetailUiState
}

@Composable
fun PokemonDetailScreen(
    uiState: PokemonDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier,
    imageModifier: Modifier = Modifier,
) {
    when (uiState) {
        PokemonDetailUiState.Loading -> PokemonDetailLoadingState(
            onBack = onBack,
            modifier = modifier,
        )
        is PokemonDetailUiState.Error -> PokemonDetailErrorState(
            message = uiState.message,
            onBack = onBack,
            onRetry = onRetry,
            modifier = modifier,
        )
        is PokemonDetailUiState.Success -> PokemonDetailContent(
            pokemon = uiState.pokemon,
            isFavorite = uiState.isFavorite,
            onBack = onBack,
            onFavoriteToggle = onFavoriteToggle,
            imageModifier = imageModifier,
            modifier = modifier,
        )
    }
}

@Composable
private fun PokemonDetailLoadingState(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        DetailBackButton(
            onBack = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
        )
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Composable
private fun PokemonDetailErrorState(
    message: String,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val errorMessage = message.ifBlank { stringResource(R.string.feature_details_error_generic) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(24.dp),
        ) {
            DetailBackButton(onBack = onBack)
            Text(
                text = stringResource(R.string.feature_details_error_title),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(onClick = onRetry) {
                Text(text = stringResource(R.string.feature_details_retry))
            }
        }
    }
}

@Composable
private fun PokemonDetailContent(
    pokemon: PokemonDetail,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onFavoriteToggle: () -> Unit,
    imageModifier: Modifier = Modifier,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DetailBackButton(onBack = onBack)
                    IconButton(onClick = onFavoriteToggle) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                AsyncImage(
                    model = pokemon.imageUrl,
                    contentDescription = pokemon.name,
                    modifier = imageModifier
                        .size(220.dp)
                        .align(Alignment.CenterHorizontally),
                    contentScale = ContentScale.Fit,
                )
                Text(
                    text = pokemon.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.feature_details_number, pokemon.id),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }

        item {
            PokemonDetailInfoCard(
                title = stringResource(R.string.feature_details_about_title),
                lines = listOf(
                    stringResource(R.string.feature_details_height, pokemon.heightMeters),
                    stringResource(R.string.feature_details_weight, pokemon.weightKilograms),
                ),
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        item {
            PokemonDetailInfoCard(
                title = stringResource(R.string.feature_details_types_title),
                lines = pokemon.types,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        item {
            PokemonDetailInfoCard(
                title = stringResource(R.string.feature_details_abilities_title),
                lines = pokemon.abilities,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        item {
            Box(modifier = Modifier.size(4.dp))
        }
    }
}

@Composable
private fun PokemonDetailInfoCard(
    title: String,
    lines: List<String>,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            lines.forEach { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Composable
private fun DetailBackButton(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onBack,
        modifier = modifier,
    ) {
        Text(text = stringResource(R.string.feature_details_back))
    }
}

@Preview(showBackground = true)
@Composable
private fun PokemonDetailScreenPreview() {
    ShowUsWhatYouGotTheme {
        PokemonDetailScreen(
            uiState = PokemonDetailUiState.Success(
                PokemonDetail(
                    id = 25,
                    name = "Pikachu",
                    imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/25.png",
                    types = listOf("Electric"),
                    abilities = listOf("Static", "Lightning Rod"),
                    heightMeters = 0.4,
                    weightKilograms = 6.0,
                )
            ),
            onBack = {},
            onRetry = {},
            onFavoriteToggle = {},
        )
    }
}

