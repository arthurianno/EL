package com.elta.android.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf

@Composable
internal fun LocalContentProvider(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalBrash provides eltaBrash,
        LocalColors provides eltaColors,
        LocalShapes provides eltaShapes,
        LocalDimens provides eltaDimens
    ) {
        content()
    }
}

internal val LocalBrash = compositionLocalOf { eltaBrash }

internal val LocalColors = compositionLocalOf { eltaColors }

internal val LocalShapes = compositionLocalOf { eltaShapes }

internal val LocalDimens = compositionLocalOf { eltaDimens }

internal val LocalTypes = compositionLocalOf { eltaTypes }

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
