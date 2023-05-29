package com.elta.android.presentation.features.profile.settings.glucoseformat

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.viewModels
import com.elta.android.domain.features.user.model.GlucoseFormat
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.core.compose.widgets.HSpacerMedium
import com.elta.android.presentation.core.compose.widgets.VSpacer
import com.elta.android.presentation.core.compose.widgets.VSpacerSmall
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBar
import com.elta.android.presentation.core.compose.widgets.buttons.DownButton
import com.elta.android.presentation.features.profile.settings.glucoseformat.model.GlucoseFormatAction
import com.elta.android.presentation.features.profile.settings.glucoseformat.viewmodel.GlucoseFormatViewModel
import com.elta.android.presentation.theme.GetLocalProperties

class GlucoseFormatFragment : BaseComposeFragment<GlucoseFormatViewModel>() {
    override val viewModel: GlucoseFormatViewModel by viewModels { viewModelFactory }

    override fun GlucoseFormatViewModel.init() {
        appTopBar.setStartIconAction(AppAction.BackPressure)
        downButton.setText(getString(R.string.profile_settings_save_changes))
    }

    @Composable
    override fun Content(viewModel: GlucoseFormatViewModel) {
        GetLocalProperties { dimens, _, _, _, _ ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                Column {
                    AppTopBar(viewModel = viewModel)
                    Column(modifier = Modifier.padding(horizontal = dimens.contentPadding)) {
                        Title()
                        VSpacerSmall()
                        VSpacer(height = dimens.glucoseFormatRadioGroupTopSpacer)
                        FormatRadioItem(
                            viewModel = viewModel,
                            selectedFormat = GlucoseFormat.PLASMA,
                            textId = R.string.profile_glucose_format_plasma
                        )
                        VSpacer(height = dimens.glucoseFormatRadioGroupSpacer)
                        FormatRadioItem(
                            viewModel = viewModel,
                            selectedFormat = GlucoseFormat.CAPILLARY,
                            textId = R.string.profile_glucose_format_caplilary
                        )
                        VSpacer(height = dimens.glucoseFormatTextTopPadding)
                        MainText()
                    }
                }
            }
            DownButton(widgetModel = viewModel.downButton)
        }
    }

    @Composable
    private fun MainText() {
        GetLocalProperties { dimens, _, _, _, _ ->
            Text(text = stringResource(id = R.string.profile_setting_glucose_format_text1))
            VSpacer(height = dimens.glucoseFormatTextBetweenPadding)
            Text(text = stringResource(id = R.string.profile_setting_glucose_format_text2))
        }
    }

    @Composable
    private fun FormatRadioItem(
        viewModel: GlucoseFormatViewModel,
        selectedFormat: GlucoseFormat,
        @StringRes textId: Int
    ) {
        val glucoseFormat = viewModel.state.collectAsState().value.profile.glucoseFormat
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .selectable(
                    selected = glucoseFormat == selectedFormat,
                    onClick = {
                        viewModel.sendAction(
                            GlucoseFormatAction.SelectFormat(selectedFormat)
                        )
                    }
                )
        ) {
            RadioButton(
                selected = glucoseFormat == selectedFormat,
                onClick = {
                    viewModel.sendAction(
                        GlucoseFormatAction.SelectFormat(selectedFormat)
                    )
                }
            )
            HSpacerMedium()
            Text(text = stringResource(id = textId))
        }
    }

    @Composable
    private fun Title() {
        GetLocalProperties { _, _, _, _, types ->
            Text(
                text = stringResource(id = R.string.profile_setting_glucose_format_title),
                style = types.h1
            )
        }
    }

    @Composable
    private fun AppTopBar(viewModel: GlucoseFormatViewModel) {
        BaseAppTopBar(
            widgetModel = viewModel.appTopBar,
            startIcon = R.drawable.ic_back
        )
    }
}
