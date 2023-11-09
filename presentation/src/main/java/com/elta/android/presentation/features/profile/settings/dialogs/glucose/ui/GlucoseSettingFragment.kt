package com.elta.android.presentation.features.profile.settings.dialogs.glucose.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.fragment.app.viewModels
import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings.Companion.NORMAL_END
import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings.Companion.NORMAL_START
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialog
import com.elta.android.presentation.features.profile.settings.dialogs.glucose.ui.widgets.GlucoseRangeSettingScreen
import com.elta.android.presentation.features.profile.settings.dialogs.glucose.viewmodels.GlucoseSettingViewModel


class GlucoseSettingFragment : BaseComposeFragment<GlucoseSettingViewModel>() {
    companion object {
        fun newInstance(): GlucoseSettingFragment = GlucoseSettingFragment()
    }

    override val viewModel: GlucoseSettingViewModel by viewModels { viewModelFactory }

    override fun GlucoseSettingViewModel.init() {
        with(viewModel) {
            appTopBar.setStartIconAction(AppAction.BackPressure)

            minBeforeMeal.setText(NORMAL_START.toString())
            maxBeforeMeal.setText(NORMAL_END.toString())
            minAfterMeal.setText(NORMAL_START.toString())
            maxAfterMeal.setText(NORMAL_END.toString())
            warningExitDialog.initDialog(
                title = getString(R.string.exit_dialog_title),
                message = getString(R.string.exit_dialog_message),
                positiveButtonText = getString(R.string.yes_text),
                negativeButtonText = getString(R.string.no_text)
            )
            downButton.setText(getString(R.string.profile_settings_glucose_action_button))
            downButton.setEnableState(false)
        }
    }

    @Composable
    override fun Dialogs(viewModel: GlucoseSettingViewModel) {
        BaseDialog(widgetModel = viewModel.warningExitDialog)
    }

    @Composable
    override fun Content(viewModel: GlucoseSettingViewModel) {
        val state = viewModel.state.collectAsState()
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (state.value.isLoading) CircularProgressIndicator()
            else GlucoseRangeSettingScreen(viewModel)
        }
    }
}
