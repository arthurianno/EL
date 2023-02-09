package com.elta.android.presentation.core.compose.widgets

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun WaveGraph(
    uri: Uri,
    isClickable: Boolean = false,
    lineColor: Color = Color.Cyan,
    timeStep: Double = 0.1,
    backgroundColor: Color = Color.Transparent,
    onClick: (position: Int) -> Unit = {},
    onPlay: (position: Int) -> Unit = {}
) {

}

@Preview
@Composable
fun PreviewWaveGraph() {
    val uri = LocalContext.current.contentResolver
}