package com.elta.android.presentation.features.consultant.ui.components.top

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.widgets.HSpacerSmall
import com.elta.android.presentation.core.compose.widgets.HSpacerVerySmall
import com.elta.android.presentation.theme.GetLocalProperties

@Composable
fun ConsultantTopBar(
    canGoBack: Boolean,
    onBackButtonClick: () -> Unit,
    onResetClick: () -> Unit
) {
    GetLocalProperties { dimens, _, colors, _, types ->
        TopAppBar(
            backgroundColor = colors.white,
            elevation = dimens.zero,
            contentPadding = dimens.consultantTopBarContentPadding,
            modifier = Modifier.statusBarsPadding()
        ) {
            if (canGoBack) {
                BackButton(onBackButtonClick)
            } else {
                HSpacerSmall()
            }
            AppIcon()
            HSpacerSmall()
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(id = R.string.consultant_topbar_title),
                        style = types.h2,
                        color = colors.blackBlue
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.img_green_dot),
                            contentDescription = null,
                            modifier = Modifier.size(8.dp)
                        )
                        HSpacerVerySmall()
                        Text(
                            text = stringResource(id = R.string.consultant_virtual_assistant),
                            style = types.caption1,
                            color = colors.shadeBlack2,
                            fontSize = 11.sp
                        )
                    }
                }
                
                Text(
                    text = stringResource(id = R.string.consultant_reset),
                    color = colors.gGreenB,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { onResetClick() }
                        .padding(8.dp)
                )
            }
        }
    }
}

@Composable
private fun BackButton(onBackButtonClick: () -> Unit) {
    GetLocalProperties { _, _, colors, _, _ ->
        IconButton(onClick = onBackButtonClick) {
            Image(
                painter = painterResource(id = R.drawable.ic_back),
                colorFilter = ColorFilter.tint(colors.blackBlue),
                contentDescription = null
            )
        }
    }
}

@Composable
private fun AppIcon() {
    Box {
        Image(
            painter = painterResource(id = R.drawable.img_round_elta),
            contentDescription = null,
            modifier = Modifier.size(36.dp)
        )
    }
}
