package com.elta.android.presentation.core.compose.widgets.animation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment

@Composable
fun VerticallyAnimation(
    visualState: Boolean,
    toUp: Boolean = true,
    content: @Composable () -> Unit
) {
    val alignmentAnimation = if (toUp) Alignment.Bottom else Alignment.Top
    AnimatedVisibility(
        visible = visualState,
        enter = expandVertically(expandFrom = alignmentAnimation),
        exit = shrinkVertically(shrinkTowards = alignmentAnimation)
    ) {
        content()
    }
}

@Composable
fun HorizontallyAnimation(
    visualState: Boolean,
    duration: Int = AnimationConstants.DefaultDurationMillis,
    toLeft: Boolean = true,
    content: @Composable () -> Unit
) {
    val alignmentAnimation = if (toLeft) Alignment.End else Alignment.Start
    AnimatedVisibility(
        visible = visualState,
        enter = expandHorizontally(
            expandFrom = alignmentAnimation,
            animationSpec = TweenSpec(durationMillis = duration)
        ),
        exit = shrinkHorizontally(
            shrinkTowards = alignmentAnimation,
            animationSpec = TweenSpec(durationMillis = duration)
        )
    ) {
        content()
    }
}
