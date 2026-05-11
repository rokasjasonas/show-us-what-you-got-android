package com.rokas.showuswhatyougot.common.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.rokas.showuswhatyougot.R // Assuming R is correctly resolved in the common module context
import com.rokas.showuswhatyougot.model.Pokemon

/**
 * Shared reusable composable for displaying a Pokemon item in lists, unifying the view
 * for both 'Feature List' and 'Feature Favorites'.
 *
 * @param pokemon The data model for the item.
 * @param isFavorite Boolean indicating if the item is currently favorited.
 * @param onClick Callback triggered when the user clicks anywhere on the row.
 * @param onFavoriteToggle Callback to handle toggling the favorite status, passing the Pokemon ID.
 * @param imageModifier Modifier to apply specifically to the subcompose image (allows feature modules to customize spacing/padding).
 */
@Composable
fun PokemonItemView(
    pokemon: Pokemon,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteToggle: (Int) -> Unit,
    modifier: Modifier = Modifier,
    imageModifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 1. Image Component
            SubcomposeAsyncImage(
                model = pokemon.imageUrl,
                contentDescription = pokemon.name,
                modifier = imageModifier.size(88.dp),
                contentScale = ContentScale.Fit,
                loading = { SharedPokemonImagePlaceholder(isLoading = true) },
                error = { SharedPokemonImagePlaceholder(isLoading = false) },
            )

            // 2. Info Component (Name and ID)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = pokemon.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                )
                // The ID format remains the same.
                Text(
                    text = "#${pokemon.id.toString().padStart(3, '0')}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            // 3. Favorite Button
            IconButton(onClick = { onFavoriteToggle(pokemon.id) }) {
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
private fun SharedPokemonImagePlaceholder(isLoading: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(28.dp))
        } else {
            // Assuming the drawable 'ic_image_placeholder' exists in the project resources
            Icon(
                painter = painterResource(id = com.rokas.showuswhatyougot.R.drawable.ic_image_placeholder),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(36.dp),
            )
        }
    }
}