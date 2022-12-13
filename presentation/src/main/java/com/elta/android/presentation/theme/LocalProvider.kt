package com.elta.android.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalContext
import com.elta.android.presentation.core.compose.common.NetworkState
import com.elta.android.presentation.core.compose.networkStateAsFlow

@Composable
internal fun LocalContentProvider(
    content: @Composable () -> Unit
) {
    val networkState =
        LocalContext.current.networkStateAsFlow()
            .collectAsState(initial = NetworkState.Unavailable).value
    CompositionLocalProvider(
        LocalBrash provides eltaBrash,
        LocalColors provides eltaColors,
        LocalShapes provides eltaShapes,
        LocalDimens provides eltaDimens,
        LocalNetworkState provides networkState
    ) {
        content()
    }
}

internal val LocalBrash = compositionLocalOf { eltaBrash }

internal val LocalColors = compositionLocalOf { eltaColors }

internal val LocalShapes = compositionLocalOf { eltaShapes }

internal val LocalDimens = compositionLocalOf { eltaDimens }

internal val LocalTypes = compositionLocalOf { eltaTypes }

internal val LocalNetworkState = compositionLocalOf<NetworkState> { NetworkState.Unavailable }

@Composable
fun GetLocalProperties(
    content: @Composable (
        dimens: EltaDimens,
        brash: EltaBrash,
        colors: EltaColors,
        shapes: EltaShapes,
        types: EltaTypes
    ) -> Unit
) {
    content(
        LocalDimens.current,
        LocalBrash.current,
        LocalColors.current,
        LocalShapes.current,
        LocalTypes.current
    )
}
