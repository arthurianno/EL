package com.elta.android.presentation.features.devices.search.widgets

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.BaseWidgetModel
import com.elta.android.presentation.features.devices.search.model.GlucometerSearchAction
import com.elta.android.presentation.features.devices.search.model.GlucometerSearchButtonStatus
import com.elta.android.presentation.features.devices.search.model.GlucometerSearchStatus
import com.elta.android.presentation.theme.GetLocalProperties

private const val CLEANER_DURATION_MILLIS = 800
private const val CONNECTION_DURATION_MILLIS = 800
private const val ROTATE_DURATION_MILLIS = 2000

@Immutable
internal data class GlucometerSearchButtonWidgetState(
    val searchStatus: GlucometerSearchStatus,
    val buttonStatus: GlucometerSearchButtonStatus,
    val clickable: Boolean
)

internal class GlucometerSearchButtonWidgetModel :
    BaseWidgetModel<GlucometerSearchButtonWidgetState>() {
    override fun createInitState(): GlucometerSearchButtonWidgetState =
        GlucometerSearchButtonWidgetState(
            searchStatus = GlucometerSearchStatus.Off,
            buttonStatus = GlucometerSearchButtonStatus.Off,
            clickable = true
        )

    fun resetSearch() {
        setState {
            state.value.copy(
                searchStatus = GlucometerSearchStatus.Off,
                buttonStatus = GlucometerSearchButtonStatus.Off,
                clickable = true
            )
        }
    }

    fun deviceConnect() {
        setState {
            state.value.copy(
                searchStatus = GlucometerSearchStatus.On,
                buttonStatus = GlucometerSearchButtonStatus.On,
                clickable = true
            )
        }
    }

    internal fun click() {
        when (state.value.searchStatus) {
            GlucometerSearchStatus.Off -> setState {
                state.value.copy(searchStatus = GlucometerSearchStatus.Connecting)
            }

            GlucometerSearchStatus.On -> sendAction(GlucometerSearchAction.StopSearch)
            else -> Unit
        }
    }

    internal fun targetCollapsed() {
        setState {
            state.value.copy(
                buttonStatus = GlucometerSearchButtonStatus.Connecting,
                clickable = false
            )
        }
        sendAction(GlucometerSearchAction.StartConnection)
    }
}

@Composable
internal fun GlucometerSearchButton(widgetModel: GlucometerSearchButtonWidgetModel) {
    val state = widgetModel.state.collectAsState()
    val isConnecting = state.value.searchStatus == GlucometerSearchStatus.Connecting &&
        state.value.buttonStatus == GlucometerSearchButtonStatus.Connecting
    GetLocalProperties { dimens, _, _, _, _ ->
        Box(
            modifier = Modifier.requiredSize(dimens.searchGlucometerBoxSize),
            contentAlignment = Alignment.Center
        ) {
            Target(state, isConnecting)
            PulseCleaner(enable = isConnecting)
            SingleCleaner(
                enable = state.value.searchStatus == GlucometerSearchStatus.Connecting && !isConnecting,
                animationFinished = widgetModel::targetCollapsed
            )
            CentralButton(state = state, onClick = widgetModel::click)
        }
    }
}

@Composable
fun SingleCleaner(
    enable: Boolean,
    animationFinished: () -> Unit
) {
    GetLocalProperties { dimens, brash, colors, shapes, types ->
        var width = remember { dimens.zero }
        if (enable) width = dimens.cleanerCircleWidth
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shapes.round)
                .background(color = Color.Transparent)
                .border(
                    animateDpAsState(
                        targetValue = width,
                        animationSpec = TweenSpec(durationMillis = CLEANER_DURATION_MILLIS),
                        finishedListener = {
                            if (it == dimens.cleanerCircleWidth) animationFinished()
                        }
                    ).value,
                    shape = shapes.round,
                    color = colors.white
                )
        )
    }
}

@Composable
fun PulseCleaner(enable: Boolean) {
    GetLocalProperties { dimens, brash, colors, shapes, types ->
        val width = remember { Animatable(dimens.cleanerCircleWidth, Dp.VectorConverter) }
        LaunchedEffect(key1 = enable) {
            if (enable) {
                width.snapTo(dimens.cleanerCircleWidth)
                width.animateTo(
                    dimens.zero,
                    animationSpec = InfiniteRepeatableSpec(
                        animation = TweenSpec(durationMillis = CONNECTION_DURATION_MILLIS),
                        repeatMode = RepeatMode.Reverse
                    )
                )
            } else {
                width.stop()
            }
        }
        if (enable) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shapes.round)
                    .background(color = Color.Transparent)
                    .border(
                        width = width.value,
                        shape = shapes.round,
                        color = colors.white
                    )
            )
        }
    }
}

@Composable
private fun Target(
    state: State<GlucometerSearchButtonWidgetState>,
    isConnecting: Boolean
) {
    val isSearch = state.value.searchStatus == GlucometerSearchStatus.On
    TargetBackground(isConnecting, isSearch)
    RotateImage(isSearch)
}

@Composable
private fun RotateImage(isSearch: Boolean) {
    val rotatePosition = remember { Animatable(0f, Float.VectorConverter) }
    LaunchedEffect(key1 = true) {
        rotatePosition.animateTo(
            360f,
            animationSpec = InfiniteRepeatableSpec(
                animation = tween(
                    durationMillis = ROTATE_DURATION_MILLIS,
                    easing = LinearEasing
                )
            )
        )
    }
    if (isSearch) {
        Image(
            painter = painterResource(id = R.drawable.find_device_background),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .rotate(rotatePosition.value)
        )
    }
}

@Composable
private fun TargetBackground(
    isConnecting: Boolean,
    isSearch: Boolean,
) {
    GetLocalProperties { dimens, _, colors, shapes, _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape = shapes.round)
                .background(
                    color = colors.gGreenB10.takeIf { isConnecting || isSearch }
                        ?: colors.paleGray
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(dimens.targetLineWidth)
                    .background(color = colors.white)
                    .align(Alignment.TopCenter)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimens.targetLineWidth)
                    .background(color = colors.white)
                    .align(Alignment.CenterStart)
            )
            Box(
                modifier = Modifier
                    .size(dimens.targetCircleSize)
                    .clip(shapes.round)
                    .background(color = Color.Transparent)
                    .border(
                        width = dimens.targetLineWidth,
                        color = colors.white,
                        shape = shapes.round
                    )
                    .align(Alignment.Center)
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
private fun CentralButton(
    state: State<GlucometerSearchButtonWidgetState>,
    onClick: () -> Unit,
) {
    val buttonStatus = state.value.buttonStatus
    val enable = state.value.clickable
    GetLocalProperties { dimens, _, colors, shapes, _ ->
        Surface(
            modifier = Modifier
                .size(dimens.searchGlucometerButtonSize),
            elevation = dimens.elevationDefault,
            shape = shapes.round,
            color = colors.gGreenB.takeIf { buttonStatus == GlucometerSearchButtonStatus.Off }
                ?: colors.white,
            enabled = enable,
            onClick = onClick,
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_search_button),
                contentDescription = null,
                colorFilter = ColorFilter.tint(
                    color = when (buttonStatus) {
                        GlucometerSearchButtonStatus.Off -> colors.white
                        GlucometerSearchButtonStatus.On -> colors.red
                        GlucometerSearchButtonStatus.Connecting -> colors.shadeBlack2
                    },
                ),
                contentScale = ContentScale.None,
            )
        }
    }
}
