package com.elta.android.presentation.core.compose.widgets

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.BaseWidgetModel

data class BaseTopAppBarState(
    val title: String = ""
)

class BaseAppTopBarWidgetModel : BaseWidgetModel<BaseTopAppBarState>(BaseTopAppBarState()) {
    private var startIconAction: Action? = null
    private var endIconAction: Action? = null

    infix fun setStartIconAction(action: Action) {
        startIconAction = action
    }

    infix fun setEndIconAction(action: Action) {
        endIconAction = action
    }

    fun setTitle(title: String) {
        setState { state.value.copy(title = title) }
    }

    fun startIconClick() {
        startIconAction?.let { sendAction(it) }
    }

    fun endIconClick() {
        endIconAction?.let { sendAction(it) }
    }
}

@Composable
fun BaseAppTopBar(
    widgetModel: BaseAppTopBarWidgetModel,
    backgroundColor: Color = MaterialTheme.colors.background,
    textStyle: TextStyle = MaterialTheme.typography.body1,
    textColor: Color = MaterialTheme.colors.onPrimary,
    @DrawableRes startIcon: Int? = null,
    startIconColor: Color = textColor,
    @DrawableRes endIcon: Int? = null,
    endIconColor: Color = textColor,
    paddingValues: PaddingValues = WindowInsets.systemBars.asPaddingValues()
) {
    val state = widgetModel.state.collectAsState()
    TopAppBar(
        title = {
            Text(
                text = state.value.title,
                style = textStyle.copy(color = textColor)
            )
        },
        backgroundColor = backgroundColor,
//        modifier = Modifier.padding(paddingValues),
        navigationIcon = {
            startIcon?.let {
                BarIconButton(
                    iconId = it,
                    color = startIconColor,
                    onClick = widgetModel::startIconClick
                )
            }
        },
        actions = {
            endIcon?.let {
                BarIconButton(
                    iconId = it,
                    color = endIconColor,
                    onClick = widgetModel::endIconClick
                )
            }
        },
        elevation = 0.dp
    )
}

@Composable
private fun BarIconButton(
    @DrawableRes iconId: Int,
    color: Color,
    contentDescription: String? = null,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(id = iconId),
            tint = color,
            contentDescription = contentDescription
        )
    }
}
