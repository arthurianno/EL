package com.elta.android.presentation.core.compose.widgets.buttons

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import com.elta.android.presentation.core.compose.widgets.HSpacerVerySmall
import com.elta.android.presentation.theme.GetLocalProperties

@Composable
fun SmallButton(
    text: String,
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    GetLocalProperties { _, brash, colors, shapes, types ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .clip(shapes.smallButton)
                .clickable(
                    role = Role.Button,
                    onClick = onClick
                )
                .background(brush = brash.smallButton)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = icon),
                    tint = colors.white,
                    contentDescription = null
                )
                HSpacerVerySmall()
                Text(
                    text = text,
                    style = types.buttonSmallText,
                    color = colors.white
                )
            }
        }
    }
}
