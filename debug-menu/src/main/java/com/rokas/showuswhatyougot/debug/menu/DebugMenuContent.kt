package com.rokas.showuswhatyougot.debug.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DebugMenuContent(
    appVersionName: String,
    appVersionCode: Int,
    analyticsEvents: List<DebugAnalyticsEntry> = emptyList(),
    onClearEvents: () -> Unit = {},
    isThrottleEnabled: Boolean = false,
    onThrottleToggle: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.debug_menu_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        }

        item { HorizontalDivider() }

        item {
            Text(
                text = stringResource(R.string.debug_menu_app_version_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            Text(
                text = stringResource(R.string.debug_menu_app_version_value, appVersionName, appVersionCode),
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.debug_menu_throttle_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Switch(
                    checked = isThrottleEnabled,
                    onCheckedChange = onThrottleToggle,
                )
            }
            Text(
                text = stringResource(R.string.debug_menu_throttle_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.debug_menu_events_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                if (analyticsEvents.isNotEmpty()) {
                    TextButton(onClick = onClearEvents) {
                        Text(text = stringResource(R.string.debug_menu_clear))
                    }
                }
            }
        }

        if (analyticsEvents.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.debug_menu_no_events),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(analyticsEvents.reversed()) { entry ->
                DebugEventRow(entry = entry)
            }
        }
    }
}

@Composable
private fun DebugEventRow(
    entry: DebugAnalyticsEntry,
    modifier: Modifier = Modifier,
) {
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = entry.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = timeFormat.format(Date(entry.timestamp)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DebugMenuContentPreview() {
    MaterialTheme {
        DebugMenuContent(
            appVersionName = "1.0",
            appVersionCode = 1,
            analyticsEvents = listOf(
                DebugAnalyticsEntry("home_screen_open", System.currentTimeMillis() - 5000),
                DebugAnalyticsEntry("pokemon_click", System.currentTimeMillis() - 2000),
                DebugAnalyticsEntry("details_screen_open", System.currentTimeMillis()),
            ),
            onClearEvents = {},
        )
    }
}

