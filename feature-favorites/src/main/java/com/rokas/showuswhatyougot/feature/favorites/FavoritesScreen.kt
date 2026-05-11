package com.rokas.showuswhatyougot.feature.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rokas.showuswhatyougot.model.Pokemon
import com.rokas.showuswhatyougot.ui.PokemonItemView

@Composable
fun FavoritesScreen(
    favorites: List<Pokemon>,
    onPokemonClick: (Int) -> Unit,
    onFavoriteToggle: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (favorites.isEmpty()) {
        FavoritesEmptyState(modifier = modifier)
    } else {
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
                        text = "Favorites",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${favorites.size} Pokémon",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            items(
                items = favorites,
                key = Pokemon::id,
            ) { pokemon ->
                // --- REFACTORED: Using the shared component ---
                PokemonItemView(
                    pokemon = pokemon,
                    isFavorite = true, // Always true here since it's in Favorites
                    onClick = { onPokemonClick(pokemon.id) },
                    onFavoriteToggle = onFavoriteToggle,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            item {
                Box(modifier = Modifier.size(4.dp))
            }
        }
    }
}

@Composable
private fun FavoritesEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Use shared composable placeholder if possible, or stick to simple structure for now
            androidx.compose.material3.Icon(
                imageVector = androidx.compose.material.icons.Icons.Filled.Favorite,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "No favorites yet",
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = "Tap the heart icon on any Pokémon to add it here",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}