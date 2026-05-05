package com.rokas.showuswhatyougot.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rokas.showuswhatyougot.common.R
import com.rokas.showuswhatyougot.ui.theme.ShowUsWhatYouGotTheme

@Composable
fun NoNetworkBanner(
    modifier: Modifier = Modifier,
    message: String = stringResource(R.string.no_network_banner_message),
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
    ) {
        Text(
            text = message,
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NoNetworkBannerPreview() {
    ShowUsWhatYouGotTheme {
        NoNetworkBanner()
    }
}

