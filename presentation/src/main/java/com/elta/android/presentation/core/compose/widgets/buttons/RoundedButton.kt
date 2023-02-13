package com.elta.android.presentation.core.compose.widgets.buttons

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.elta.android.presentation.theme.LocalColors
import com.elta.android.presentation.theme.LocalDimens
import com.elta.android.presentation.theme.LocalShapes

@Composable
fun RoundedButton(
    @DrawableRes icon: Int,
    iconColor: Color = Color.Unspecified,
    background: Color = LocalColors.current.white,
    border: Color? = LocalColors.current.shadeBlack3,
    size: Dp = LocalDimens.current.roundedButton,
    onClick: () -> Unit
) {
    val shapes = LocalShapes.current
    Box(
        modifier = Modifier
            .requiredSize(size)
            .clip(shape = shapes.round)
            .clickable(onClick = onClick)
            .then(border?.let {
                Modifier
                    .border(
                        width = LocalDimens.current.borderWidth,
                        color = border,
                        shape = shapes.round
                    )
            } ?: Modifier)
            .background(
                color = background,
                shape = shapes.round
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = iconColor
        )
    }
}
