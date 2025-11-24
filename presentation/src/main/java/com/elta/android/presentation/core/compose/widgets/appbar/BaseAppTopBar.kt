package com.elta.android.presentation.core.compose.widgets.appbar

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalAbsoluteElevation
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.BaseWidgetModel
import com.elta.android.presentation.core.compose.widgets.HSpacerMedium
import com.elta.android.presentation.core.compose.widgets.HSpacerVerySmall
import com.elta.android.presentation.theme.GetLocalProperties
import com.elta.android.presentation.theme.eltaColors
import com.elta.android.presentation.theme.eltaTypes

private const val EMPTY_STRING = ""

@Immutable
data class BaseTopAppBarWidgetState(
    val title: String,
    val endText: Int?
)

class BaseAppTopBarWidgetModel : BaseWidgetModel<BaseTopAppBarWidgetState>() {
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

    fun setEndText(textId: Int?){
        setState { state.value.copy(endText = textId) }
    }

    override fun createInitState(): BaseTopAppBarWidgetState =
        BaseTopAppBarWidgetState(
            title = EMPTY_STRING,
            endText = null
        )
}

@Composable
fun BaseAppTopBar(
    widgetModel: BaseAppTopBarWidgetModel,
    backgroundColor: Color = eltaColors.white,
    textStyle: TextStyle = eltaTypes.toolBar,
    textColor: Color = eltaColors.shadeBlack1,
    @DrawableRes startIcon: Int? = null,
    startIconColor: Color = textColor,
    @DrawableRes endIcon: Int? = null,
    endIconColor: Color = textColor,
    @StringRes endText: Int? = null
) {
    val state = widgetModel.state.collectAsState()

    val endTextId = state.value.endText.takeIf { it != null }
        ?: endText

    TopAppBar(
        title = {
            Text(
                text = state.value.title,
                style = textStyle.copy(color = textColor)
            )
        },
        backgroundColor = backgroundColor,
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
            BarIconOrTextButton(
                iconId = endIcon,
                iconColor = endIconColor,
                onClick = widgetModel::endIconClick,
                text = endTextId,
                textColor = textColor,
                textStyle = textStyle
            )
        },
        elevation = LocalAbsoluteElevation.current
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

@Composable
private fun BarIconOrTextButton(
    @DrawableRes iconId: Int?,
    iconColor: Color,
    contentDescription: String? = null,
    @StringRes text: Int? = null,
    textStyle: TextStyle = MaterialTheme.typography.body1,
    textColor: Color = MaterialTheme.colors.onPrimary,
    onClick: () -> Unit
) {
    if (text == null && iconId == null) return
    GetLocalProperties { dimens, _, _, _, _ ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .defaultMinSize(dimens.clickableAreaMinSize, dimens.clickableAreaMinSize)
                .clickable(onClick = onClick)
        ) {
            HSpacerMedium()
            iconId?.let {
                Icon(
                    painter = painterResource(id = it),
                    tint = iconColor,
                    contentDescription = contentDescription
                )
                text?.let { HSpacerVerySmall() }
            }
            text?.let {
                Text(
                    text = stringResource(id = it),
                    color = textColor,
                    style = textStyle
                )
            }
            HSpacerMedium()
        }
    }
}
