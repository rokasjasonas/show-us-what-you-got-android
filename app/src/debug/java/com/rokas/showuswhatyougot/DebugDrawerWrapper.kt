package com.rokas.showuswhatyougot

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.rokas.showuswhatyougot.debug.menu.DebugAnalyticsProvider
import com.rokas.showuswhatyougot.debug.menu.DebugMenuContent
import com.rokas.showuswhatyougot.debug.menu.NetworkThrottleConfig
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch

@EntryPoint
@InstallIn(SingletonComponent::class)
interface DebugDrawerEntryPoint {
    fun debugAnalyticsProvider(): DebugAnalyticsProvider
    fun networkThrottleConfig(): NetworkThrottleConfig
}

@Composable
fun DebugDrawerWrapper(
    content: @Composable () -> Unit,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val context = LocalContext.current
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val scope = rememberCoroutineScope()
    val entryPoint = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            DebugDrawerEntryPoint::class.java,
        )
    }
    val debugAnalyticsProvider = remember { entryPoint.debugAnalyticsProvider() }
    val throttleConfig = remember { entryPoint.networkThrottleConfig() }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(300.dp)) {
                DebugMenuContent(
                    appVersionName = packageInfo.versionName ?: "unknown",
                    appVersionCode = packageInfo.versionCode,
                    analyticsEvents = debugAnalyticsProvider.events,
                    onClearEvents = { debugAnalyticsProvider.clear() },
                    isThrottleEnabled = throttleConfig.enabled.value,
                    onThrottleToggle = { throttleConfig.enabled.value = it },
                )
            }
        },
    ) {
        Box {
            content()
            FloatingActionButton(
                onClick = { scope.launch { drawerState.open() } },
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .align(Alignment.TopStart)
                    .padding(start = 16.dp, bottom = 16.dp)
                    .size(40.dp),
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_debug),
                    contentDescription = "Open debug menu",
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

