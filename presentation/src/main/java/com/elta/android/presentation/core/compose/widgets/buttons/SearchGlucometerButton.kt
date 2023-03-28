package com.elta.android.presentation.core.compose.widgets.buttons

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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.BaseWidgetModel
import com.elta.android.presentation.theme.GetLocalProperties

@Immutable
data class SearchGlucometerButtonWidgetState(
    val searchStatus: SearchGlucometerStatus,
    val isClickable: Boolean
)

enum class SearchGlucometerStatus {
    Off,
    On,
    Connecting
}

class SearchGlucometerButtonWidgetModel : BaseWidgetModel<SearchGlucometerButtonWidgetState>() {
    override fun createInitState(): SearchGlucometerButtonWidgetState =
        SearchGlucometerButtonWidgetState(
            searchStatus = SearchGlucometerStatus.Off,
            isClickable = true
        )

    fun click() {}
}

@Composable
fun SearchGlucometerButton(widgetModel: SearchGlucometerButtonWidgetModel) {
    GetLocalProperties { dimens, brash, colors, shapes, types ->
        Box(
            modifier = Modifier.requiredSize(dimens.searchGlucometerBoxSize),
            contentAlignment = Alignment.Center
        ) {
            Target()
            CentralButton(widgetModel)
        }
    }
}

@Composable
fun Target() {
    GetLocalProperties { dimens, _, colors, shapes, _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape = shapes.round)
                .background(color = colors.paleGray)
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
    widgetModel: SearchGlucometerButtonWidgetModel
) {
    val state = widgetModel.state.collectAsState()
    val isEnable = state.value.isClickable
    GetLocalProperties { dimens, _, colors, shapes, _ ->
        Surface(
            modifier = Modifier
                .size(dimens.searchGlucometerButtonSize),
            elevation = dimens.elevationDefault,
            shape = shapes.round,
            color = colors.gGreenB.takeIf { isEnable } ?: colors.white,
            enabled = isEnable,
            onClick = widgetModel::click
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_search_button),
                contentDescription = null,
                colorFilter = ColorFilter.tint(
                    color = colors.paleGray.takeIf { isEnable }
                        ?: colors.shadeBlack3
                ),
                contentScale = ContentScale.None
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SearchButtonPreview() {
    val widgetModel = SearchGlucometerButtonWidgetModel()
    SearchGlucometerButton(widgetModel = widgetModel)
}
