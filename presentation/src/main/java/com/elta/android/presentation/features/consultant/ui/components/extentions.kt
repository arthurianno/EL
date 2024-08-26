package com.elta.android.presentation.features.consultant.ui.components

import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp

@Composable
fun animateMessageShapeAsState(
    condition: Boolean,
    targetCornerRadius: Dp,
    defaultCornerRadius: Dp,
    animationSpec: TweenSpec<Dp> = tween(0)
) = animateDpAsState(
    targetValue = if (condition) targetCornerRadius else defaultCornerRadius,
    animationSpec = animationSpec
)
