package com.elta.android.presentation.core.compose.widgets

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun VSpacerVerySmall() {
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
fun VSpacerSmall() {
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun VSpacerHalfMedium() {
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
fun VSpacerMedium() {
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun VSpacerHalfLarge() {
    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
fun VSpacerLarge() {
    Spacer(modifier = Modifier.height(32.dp))
}

@Composable
fun VSpacer(height: Int) {
    Spacer(modifier = Modifier.height(height.dp))
}

@Composable
fun VSpacer(height: Dp) {
    Spacer(modifier = Modifier.height(height))
}

@Composable
fun HSpacerVerySmall() {
    Spacer(modifier = Modifier.width(4.dp))
}

@Composable
fun HSpacerSmall() {
    Spacer(modifier = Modifier.width(8.dp))
}

@Composable
fun HSpacerHalfMedium() {
    Spacer(modifier = Modifier.width(16.dp))
}

@Composable
fun HSpacerMedium() {
    Spacer(modifier = Modifier.width(16.dp))
}

@Composable
fun HSpacerLarge() {
    Spacer(modifier = Modifier.width(32.dp))
}

@Composable
fun HSpacer(width: Int) {
    Spacer(modifier = Modifier.width(width.dp))
}

@Composable
fun HSpacer(width: Dp) {
    Spacer(modifier = Modifier.width(width))
}
