package com.rokas.showuswhatyougot.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.rokas.showuswhatyougot.common.R
import com.rokas.showuswhatyougot.model.Pokemon

/**
 * Shared reusable composable for displaying a Pokemon item in lists.
 * This replaces the logic previously found in `PokemonListScreen.kt`.
 *
 * @param pokemon The data model for the item.
 * @param isFavorite Boolean indicating if the item is currently favorited.
 * @param onClick Callback triggered when the user clicks anywhere on the row.
 * @param onFavoriteToggle Callback to handle toggling the favorite status, passing the Pokemon ID.
 * @param imageModifier Modifier to apply specifically to the subcompose image.
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
                    SharedPokemonImagePlaceholder(isLoading = true)
                },
                error = {
                    SharedPokemonImagePlaceholder(isLoading = false)
                },
            )

            Column(
                modifier = Modifier.weight(1f),
                // Spacer is used here to replicate the vertical spacing logic from the original component (which used Arrangement.spacedBy in a parent column, but I'll stick to standard layout flow for simplicity and safety).
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // The name is generally displayed with title/semi-bold, so we keep it as a simple Text composable.
                Text(
                    text = pokemon.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                // The ID format remains the same.
                Text(
                    text = "#${pokemon.id.toString().padStart(3, '0')}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

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
            Icon(
                painter = painterResource(id = R.drawable.ic_image_placeholder),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(36.dp),
            )
        }
    }
}