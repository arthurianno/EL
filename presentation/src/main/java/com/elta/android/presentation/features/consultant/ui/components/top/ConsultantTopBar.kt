package com.elta.android.presentation.features.consultant.ui.components.top

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.NetworkState
import com.elta.android.presentation.core.compose.widgets.HSpacerSmall
import com.elta.android.presentation.core.compose.widgets.HSpacerVerySmall
import com.elta.android.presentation.features.consultant.model.ConnectState
import com.elta.android.presentation.theme.GetLocalProperties
import com.elta.android.presentation.theme.LocalNetworkState

@Composable
fun ConsultantTopBar(
    connectState: ConnectState,
    onBackButtonClick: () -> Unit
) {
    GetLocalProperties { dimens, _, colors, _, _ ->
        TopAppBar(
            backgroundColor = colors.white,
            elevation = dimens.zero,
            contentPadding = dimens.consultantTopBarContentPadding,
            modifier = Modifier.statusBarsPadding()
        ) {
            BackButton(onBackButtonClick)
            AppIcon(connectState)
            HSpacerSmall()
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TopBarText(connectState)
            }
        }
    }
}

@Composable
private fun TopBarText(connectState: ConnectState) {
    val networkState = LocalNetworkState.current
    val topBarText = if (networkState == NetworkState.Unavailable) {
        stringResource(id = R.string.consultant_offline)
    } else {
        connectState.getTopBarText()
    }
    GetLocalProperties { dimens, _, colors, _, types ->
        Column {
            Text(
                text = stringResource(id = R.string.consultant_topbar_title),
                style = types.h2,
                color = colors.blackBlue
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                when {
                    connectState == ConnectState.Connecting -> CircularProgressIndicator(
                        strokeWidth = dimens.progressSmallWidth,
                        color = colors.shadeBlack1,
                        modifier = Modifier.size(dimens.consultantTopBarProgress)
                    )

                    connectState == ConnectState.Offline ||
                            networkState == NetworkState.Unavailable -> Image(
                        painter = painterResource(id = R.drawable.ic_red_alert),
                        contentDescription = null
                    )

                    else -> Unit
                }
                HSpacerVerySmall()
                Text(
                    text = topBarText,
                    style = types.caption1,
                    color = colors.shadeBlack1
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
private fun AppIcon(connectState: ConnectState) {
    val networkState = LocalNetworkState.current
    Box {
        Image(
            painter = painterResource(id = R.drawable.img_round_elta),
            contentDescription = null
        )
        if (connectState == ConnectState.Connect && networkState == NetworkState.Available) {
            Image(
                painter = painterResource(id = R.drawable.img_green_dot),
                contentDescription = null,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    }
}

// fixme переделать в экстешн который только выдаёт ид
@Composable
private fun ConnectState.getTopBarText() =
    stringResource(
        id = when (this) {
            ConnectState.Connecting -> R.string.consultant_connecting
            ConnectState.Connect -> R.string.consultant_connect
            ConnectState.Offline -> R.string.consultant_offline
        }
    )

@Preview
@Composable
private fun PreviewConsultantTopBar() {
    ConsultantTopBar(
        connectState = ConnectState.Connect,
        onBackButtonClick = {}
    )
}
