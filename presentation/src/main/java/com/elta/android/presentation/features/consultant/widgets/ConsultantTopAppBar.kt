package com.elta.android.presentation.features.consultant.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseWidgetModel
import com.elta.android.presentation.core.compose.common.NetworkState
import com.elta.android.presentation.core.compose.widgets.HSpacerSmall
import com.elta.android.presentation.core.compose.widgets.HSpacerVerySmall
import com.elta.android.presentation.features.consultant.model.ConnectState
import com.elta.android.presentation.features.consultant.model.ConsultantAction
import com.elta.android.presentation.theme.GetLocalProperties
import com.elta.android.presentation.theme.LocalNetworkState

internal data class ConsultantTopAppBarWidgetState(
    val connectState: ConnectState
)

internal class ConsultantTopAppBarWidgetModel() :
    BaseWidgetModel<ConsultantTopAppBarWidgetState>() {
    override fun createInitState(): ConsultantTopAppBarWidgetState =
        ConsultantTopAppBarWidgetState(
            connectState = ConnectState.Connecting
        )

    fun setConnectState(connectState: ConnectState) {
        setState { state.value.copy(connectState = connectState) }
    }
}

@Composable
internal fun ConsultantTopAppBar(widgetModel: ConsultantTopAppBarWidgetModel) {
    val state = widgetModel.state.collectAsState()
    val connectState = state.value.connectState
    GetLocalProperties { dimens, _, colors, _, _ ->
        TopAppBar(
            backgroundColor = colors.white,
            elevation = dimens.zero,
            contentPadding = dimens.consultantTopBarContentPadding,
            modifier = Modifier.statusBarsPadding()
        ) {
            BackButton(widgetModel)
            AppIcon(connectState)
            HSpacerSmall()
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TopBarText(connectState)
                FindButton(widgetModel)
            }
        }
    }
}

@Composable
private fun FindButton(widgetModel: ConsultantTopAppBarWidgetModel) {
    GetLocalProperties { _, _, colors, _, _ ->
        IconButton(onClick = { widgetModel.sendAction(ConsultantAction.SearchClick) }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_search2),
                contentDescription = null,
                tint = colors.blackBlue
            )
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

                    else -> {}
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
private fun BackButton(widgetModel: ConsultantTopAppBarWidgetModel) {
    GetLocalProperties { _, _, colors, _, _ ->
        IconButton(onClick = { widgetModel.sendAction(AppAction.BackPressure) }) {
            Image(
                painter = painterResource(id = R.drawable.ic_back),
                colorFilter = ColorFilter.tint(colors.blackBlue),
                contentDescription = null
            )
        }
    }
}

@Composable
private fun ConnectState.getTopBarText() =
    stringResource(
        id = when (this) {
            ConnectState.Connecting -> R.string.consultant_connecting
            ConnectState.Connect -> R.string.consultant_connect
            ConnectState.Offline -> R.string.consultant_offline
        }
    )

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
