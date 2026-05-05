package com.rokas.showuswhatyougot.debug.menu

import androidx.compose.runtime.mutableStateOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkThrottleConfig @Inject constructor() {
    val enabled = mutableStateOf(false)
    val delayMillis: Long = 3000L
}

