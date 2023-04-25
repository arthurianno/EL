package com.elta.android.presentation.core.compose.widgets.buttons

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource

@Composable
fun ButtonCircle(
    @DrawableRes icon: Int,
    @StringRes contentDescriptionId: Int,
    tint: Color? = null,
    enable: Boolean = true,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        enabled = enable
    ) {
        Icon(
            painter = painterResource(id = icon),
            tint = tint ?: Color.Unspecified,
            contentDescription = stringResource(id = contentDescriptionId)
        )
    }
}
