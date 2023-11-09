package com.elta.android.presentation.features.profile.settings.dialogs.glucose.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.clickableWithNoRipple
import com.elta.android.presentation.core.compose.widgets.VSpacer
import com.elta.android.presentation.core.compose.widgets.VSpacerLarge
import com.elta.android.presentation.core.compose.widgets.VSpacerMedium
import com.elta.android.presentation.core.compose.widgets.VSpacerSmall
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBar
import com.elta.android.presentation.core.compose.widgets.buttons.DownButton
import com.elta.android.presentation.features.profile.settings.dialogs.glucose.ui.widgets.components.GlucoseLevelCard
import com.elta.android.presentation.features.profile.settings.dialogs.glucose.ui.widgets.components.RemarkLabel
import com.elta.android.presentation.features.profile.settings.dialogs.glucose.viewmodels.GlucoseSettingViewModel
import com.elta.android.presentation.theme.GetLocalProperties

@Composable
fun GlucoseRangeSettingScreen(viewModel: GlucoseSettingViewModel) {
    GetLocalProperties { dimens, _, colors, _, _ ->
        val focusManager = LocalFocusManager.current

        Scaffold(
            scaffoldState = rememberScaffoldState(),
            modifier = Modifier
                .statusBarsPadding()
                .clickableWithNoRipple {
                    focusManager.clearFocus()
                },
            topBar = {
                BaseAppTopBar(
                    widgetModel = viewModel.appTopBar,
                    startIcon = R.drawable.ic_back,
                    startIconColor = colors.blackBlue
                )
            },
            bottomBar = {
                DownButton(viewModel.downButton)
            }
        ) {
            VSpacerLarge()
            Column(
                modifier = Modifier
                    .padding(it)
                    .padding(horizontal = dimens.contentPadding)
            ) {
                Title()
                MainContent(viewModel)
                VSpacerMedium()
                RemarkLabel()
            }
        }
    }
}

@Composable
private fun MainContent(viewModel: GlucoseSettingViewModel) {
    val state = viewModel.state.collectAsState()
    GetLocalProperties { _, _, colors, _, types ->
        VSpacer(20.dp)
        Text(
            text = stringResource(id = R.string.profile_settings_glucose_description),
            style = types.subtitle1,
            color = colors.blackBlue
        )
        VSpacerMedium()
        GlucoseLevelCard(
            title = stringResource(id = R.string.profile_settings_glucose_level_before_meal),
            errorType = state.value.errorTypeBeforeMeal,
            minWidgetModel = viewModel.minBeforeMeal,
            maxWidgetModel = viewModel.maxBeforeMeal
        )
        VSpacerMedium()
        GlucoseLevelCard(
            title = stringResource(id = R.string.profile_settings_glucose_level_after_meal),
            errorType = state.value.errorTypeAfterMeal,
            minWidgetModel = viewModel.minAfterMeal,
            maxWidgetModel = viewModel.maxAfterMeal,
            imeAction = ImeAction.Done
        )
    }

}

@Composable
private fun Title() {
    GetLocalProperties { _, _, colors, _, types ->
        Text(
            text = stringResource(id = R.string.profile_settings_glucose_title),
            style = types.h1,
            color = colors.blackBlue
        )
        VSpacerSmall()
        Text(
            text = stringResource(id = R.string.profile_settings_glucose_subtitle),
            style = types.subtitle1,
            color = colors.shadeBlack1
        )
    }
}

@Preview
@Composable
private fun PreviewTitle() {
    Column(modifier = Modifier
        .background(color = Color.White)
        .padding(8.dp)) {
        Title()
    }
}
