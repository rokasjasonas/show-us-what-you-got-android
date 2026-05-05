package com.rokas.showuswhatyougot

import androidx.compose.runtime.Composable

@Composable
fun DebugDrawerWrapper(
    content: @Composable () -> Unit,
) {
    content()
}

