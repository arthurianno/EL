package com.elta.android.presentation.core.compose.widgets

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseWidgetModel

data class BaseTopAppBarState(
    val title: String = ""
)

class BaseAppTopBarWidgetModel : BaseWidgetModel<BaseTopAppBarState>(BaseTopAppBarState()) {

    fun setTitle(title: String) {
        setState { state.value.copy(title = title) }
    }

    fun startIconClick() {
        sendAction(AppAction.BackPressure)
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
        modifier = Modifier.padding(paddingValues),
        navigationIcon = {
            startIcon?.let {
                IconButton(onClick = widgetModel::startIconClick) {
                    Icon(
                        painter = painterResource(id = it),
                        tint = startIconColor,
                        contentDescription = null
                    )
                }
            }
        },
        actions = {
            endIcon?.let {
                Icon(
                    painter = painterResource(id = it),
                    tint = endIconColor,
                    contentDescription = null
                )
            }
        },
        elevation = 0.dp
    )
}
