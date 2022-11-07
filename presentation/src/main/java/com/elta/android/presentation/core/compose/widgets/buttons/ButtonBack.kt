package com.elta.android.presentation.core.compose.widgets.buttons

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import com.elta.android.presentation.R

@Composable
fun ButtonBack(
    background: Color = Color.Transparent,
    color: Color = MaterialTheme.colors.onPrimary,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick, modifier = modifier.background(color = background)) {
        Image(
            painter = painterResource(id = R.drawable.ic_back),
            colorFilter = ColorFilter.tint(color = color),
            contentDescription = null
        )
    }
}
