package com.elta.android.presentation.core.compose.widgets.buttons

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource

@Composable
fun ButtonCircle(
    @DrawableRes icon: Int,
    tint: Color = Color.Unspecified,
    enable: Boolean = true,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        enabled = enable
    ) {
        Image(
            painter = painterResource(id = icon),
            colorFilter = ColorFilter.tint(color = tint),
            contentDescription = null
        )
    }
}
