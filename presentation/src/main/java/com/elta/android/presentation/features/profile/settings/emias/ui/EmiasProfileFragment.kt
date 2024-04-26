package com.elta.android.presentation.features.profile.settings.emias.ui

import android.os.Bundle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elta.android.common.utils.CommonFormats.FORMAT_ONLY_DIGITS
import com.elta.android.domain.features.emias.model.Emias
import com.elta.android.domain.features.emias.model.EmiasStatus
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.clickableWithNoRipple
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.core.compose.disableClickable
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBar
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBarWidgetModel
import com.elta.android.presentation.core.compose.widgets.buttons.DownButton
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialog
import com.elta.android.presentation.features.profile.settings.emias.component.EmiasProfileContent
import com.elta.android.presentation.features.profile.settings.emias.model.EmiasProfileAction
import com.elta.android.presentation.features.profile.settings.emias.viewmodel.EmiasProfileViewModel
import com.elta.android.presentation.theme.GetLocalProperties
import com.nullgr.core.date.toStringWithFormat

class EmiasProfileFragment : BaseComposeFragment<EmiasProfileViewModel>() {
    companion object {
        const val OMS_KEY_EXTRA = "oms_number"
        const val BIRTH_DATE_KEY_EXTRA = "date_birth"
        const val LINK_STATUS_KEY_EXTRA = "link_status"

        fun newInstance(linkedStatus: EmiasStatus, emias: Emias?) =
            EmiasProfileFragment().apply {
                arguments = Bundle().apply {
                    emias?.let {
                        putString(OMS_KEY_EXTRA, emias.oms)
                        putString(
                            BIRTH_DATE_KEY_EXTRA,
                            emias.birthdayDate.toStringWithFormat(FORMAT_ONLY_DIGITS)
                        )
                    }
                    putString(LINK_STATUS_KEY_EXTRA, linkedStatus.name)
                }
            }
    }

    override val viewModel: EmiasProfileViewModel by viewModels { viewModelFactory }

    override fun EmiasProfileViewModel.init() {
        saveButton.setText(getString(R.string.emias_save_button))
        saveButton.visibilityState(false)
        saveButton.setEnableState(false)
        omsInput.setHint(getString(R.string.on_boarding_emias_hint))
        omsInput.setDescription(getString(R.string.on_boarding_emias_helper))
        dateInput.setHint(
            hint = getString(R.string.on_boarding_emias_date_birth_hint),
            hintInFocus = getString(R.string.on_boarding_emias_date_birth_mask_hint)
        )
        appTopBar.setEndIconAction(EmiasProfileAction.UnbindEmias)
        appTopBar.setStartIconAction(AppAction.BackPressure)

        warningExitDialog.initDialog(
            title = getString(R.string.emias_exit_title),
            message = getString(R.string.emias_exit_message),
            positiveButtonText = getString(R.string.yes_text),
            negativeButtonText = getString(R.string.no_text)
        )

        warningEmiasUnbindDialog.initDialog(
            title = getString(R.string.emias_unbind_title),
            message = getString(R.string.emias_unbind_message),
            positiveButtonText = getString(R.string.yes_text),
            negativeButtonText = getString(R.string.no_text)
        )

        userNotFoundDialog.initDialog(
            title = getString(R.string.emias_user_not_found_title),
            message = getString(R.string.emias_user_not_found_message),
            positiveButtonText = getString(R.string.ok)
        )

        emiasProfileUnbindedDialog.initDialog(
            title = getString(R.string.emias_profile_unbinded_title),
            message = "",
            positiveButtonText = getString(R.string.ok)
        )

        internalServerErrorDialog.initDialog(
            title = getString(R.string.emias_internal_error_title),
            message = "",
            positiveButtonText = getString(R.string.ok)
        )

        agreementNotFoundDialog.initDialog(
            title = getString(R.string.emias_agreement_not_found_title),
            message = getString(R.string.emias_agreement_not_found_message),
            positiveButtonText = getString(R.string.ok)
        )

        omsAlreadyLinkedDialog.initDialog(
            title = getString(R.string.emias_oms_already_linked_title),
            message = getString(R.string.emias_oms_already_linked_message),
            positiveButtonText = getString(R.string.ok)
        )

        networkConnectionErrorDialog.initDialog(
            title = getString(R.string.emias_network_connection_error_title),
            message = getString(R.string.emias_try_later_message),
            positiveButtonText = getString(R.string.ok)
        )
    }

    @Composable
    override fun Dialogs(viewModel: EmiasProfileViewModel) {
        BaseDialog(widgetModel = viewModel.warningExitDialog)
        BaseDialog(widgetModel = viewModel.warningEmiasUnbindDialog)
        BaseDialog(widgetModel = viewModel.userNotFoundDialog)
        BaseDialog(widgetModel = viewModel.internalServerErrorDialog)
        BaseDialog(widgetModel = viewModel.emiasProfileUnbindedDialog)
        BaseDialog(widgetModel = viewModel.agreementNotFoundDialog)
        BaseDialog(widgetModel = viewModel.omsAlreadyLinkedDialog)
        BaseDialog(widgetModel = viewModel.networkConnectionErrorDialog)
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    override fun Content(viewModel: EmiasProfileViewModel) {
        val state = viewModel.state.collectAsState().value

        val keyboardController = LocalSoftwareKeyboardController.current
        val focusManager = LocalFocusManager.current
        val modifier = if (state.isLoading) Modifier.disableClickable()
        else Modifier.clickableWithNoRipple {
            keyboardController?.hide()
            focusManager.clearFocus()
            viewModel sendAction AppAction.FreeScreenTap
        }

        GetLocalProperties { _, _, colors, _, _ ->
            Box(modifier = Modifier.fillMaxSize()) {
                Scaffold(
                    scaffoldState = rememberScaffoldState(),
                    modifier = modifier.statusBarsPadding(),
                    topBar = { AppBar(viewModel.appTopBar) },
                    bottomBar = { DownButton(viewModel.saveButton) }
                ) { paddingValues ->
                    EmiasProfileContent(
                        modifier = Modifier.padding(paddingValues),
                        omsInput = viewModel.omsInput,
                        dateInput = viewModel.dateInput,
                        state = state
                    )
                }
                AnimatedVisibility (
                    state.isLoading,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colors.black.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .fillMaxSize()
                                .wrapContentWidth(Alignment.CenterHorizontally)
                                .wrapContentHeight(Alignment.CenterVertically),
                            color = colors.white
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun AppBar(appTopBarWidgetModel: BaseAppTopBarWidgetModel) {
        BaseAppTopBar(
            widgetModel = appTopBarWidgetModel,
            startIcon = R.drawable.ic_back
        )
    }

    @Preview
    @Composable
    fun PreviewEmiasProfileContent() {
        Box {
            Content(viewModel = viewModel())
        }
    }
}
