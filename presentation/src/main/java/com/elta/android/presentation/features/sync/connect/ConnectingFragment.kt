package com.elta.android.presentation.features.sync.connect

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.viewModels
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.core.compose.widgets.HSpacerSmall
import com.elta.android.presentation.core.compose.widgets.VSpacer
import com.elta.android.presentation.core.compose.widgets.VSpacerMedium
import com.elta.android.presentation.core.compose.widgets.VSpacerSmall
import com.elta.android.presentation.core.compose.widgets.buttons.DownButton
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialog
import com.elta.android.presentation.core.compose.widgets.snackbar.BaseSnackBar
import com.elta.android.presentation.core.ui.fragment.addOnBackPressedCallback
import com.elta.android.presentation.features.sync.connect.model.ConnectAction
import com.elta.android.presentation.features.sync.connect.model.ConnectMainEvent
import com.elta.android.presentation.features.sync.connect.model.ConnectingStageType
import com.elta.android.presentation.features.sync.connect.viewmodel.ConnectingViewModel
import com.elta.android.presentation.features.sync.connect.widgets.AppTopBar
import com.elta.android.presentation.features.sync.connect.widgets.HelpBottomSheet
import com.elta.android.presentation.features.sync.connect.widgets.MainImage
import com.elta.android.presentation.theme.GetLocalProperties
import com.elta.android.presentation.utils.bundle

class ConnectingFragment : BaseComposeFragment<ConnectingViewModel>() {
    companion object {
        fun newInstance(
            isOnBoarding: Boolean,
            pin: Int,
            name: String
        ) = ConnectingFragment().apply {
            arguments = bundle(
                IS_ON_BOARDING_ARGUMENT_NAME to isOnBoarding,
                PIN_ARGUMENT_NAME to pin,
                GLUCOMETER_NAME_ARGUMENT_NAME to name
            )
        }
    }

    override val viewModel: ConnectingViewModel by viewModels { viewModelFactory }

    override fun ConnectingViewModel.init() {
        connectByPinButton.setText(getString(R.string.sync_connect_by_pin_boton_text))
        connectRepeatButton.setText(getString(R.string.repeat_connect_button_text))
        syncRepeatButton.setText(getString(R.string.repeat_sync_button_text))
        searchRepeatButton.setText(getString(R.string.repeat_search_button_text))
        completeButton.setText(getString(R.string.sync_state_action_sync_completed))
        appTopBar.setStartIconAction(AppAction.BackPressure)
        appTopBar.setEndIconAction(ConnectAction.NeedHelp)
        exitDialogFromConnecting.initDialog(
            message = getString(R.string.sync_connection_exit_from_connecting_dialog_text),
            positiveButtonText = getString(R.string.yes_text),
            negativeButtonText = getString(R.string.cancel_text)
        )
        exitDialogFromSync.initDialog(
            message = getString(R.string.sync_connection_exit_from_sync_dialog_text),
            positiveButtonText = getString(R.string.yes_text),
            negativeButtonText = getString(R.string.cancel_text)
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        addOnBackPressedCallback { viewModel.backClick() }
    }

    @Composable
    override fun Dialogs(viewModel: ConnectingViewModel) {
        BaseDialog(widgetModel = viewModel.exitDialogFromConnecting)
        BaseDialog(widgetModel = viewModel.exitDialogFromSync)
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun Content(viewModel: ConnectingViewModel) {
        val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
        val state = viewModel.state.collectAsState().value
        val event = viewModel.event.collectAsState(initial = null).value
        LaunchedEffect(key1 = event) {
            when (event) {
                is ConnectMainEvent.ShowSheet -> sheetState.show()
                is ConnectMainEvent.HideSheet -> sheetState.hide()
                else -> Unit
            }
        }
        GetLocalProperties { _, _, _, shapes, _ ->
            ModalBottomSheetLayout(
                sheetState = sheetState,
                sheetContent = {
                    HelpBottomSheet(
                        downButtonModel = viewModel.connectByPinButton,
                        closeOnClick = {
                            viewModel sendAction ConnectAction.CloseHelp
                        }
                    )
                },
                sheetShape = shapes.sheet,
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding()
                ) {
                    Header(viewModel, state.stageType)
                    Footer(
                        viewModel = viewModel,
                        stageType = state.stageType,
                        glucometerName = state.glucometerName
                    )
                }
            }
        }
    }

    @Composable
    private fun ColumnScope.Header(
        viewModel: ConnectingViewModel,
        stageType: ConnectingStageType
    ) {
        val isCompleted =
            stageType == ConnectingStageType.Complete || stageType == ConnectingStageType.Sync
        AppTopBar(
            appTopBarModel = viewModel.appTopBar,
            startIcon = R.drawable.ic_back.takeUnless { stageType == ConnectingStageType.Complete },
            endText = R.string.sync_connect_type_button_any_difficulties.takeUnless { isCompleted }
        )
        MainImage(
            imageId = if (isCompleted) {
                R.drawable.ic_connect_finish
            } else {
                R.drawable.ic_connect_dev
            }
        )
    }

    @Composable
    private fun Footer(
        viewModel: ConnectingViewModel,
        stageType: ConnectingStageType,
        glucometerName: String
    ) {
        when (stageType) {
            ConnectingStageType.Connecting -> ConnectingFooter()
            ConnectingStageType.DeviceNotFound -> DeviceNotFoundFooter(viewModel)
            ConnectingStageType.ErrorConnect -> ErrorConnectFooter(viewModel)
            ConnectingStageType.Sync -> SyncFooter(glucometerName)
            ConnectingStageType.Complete -> CompleteFooter(viewModel)
            ConnectingStageType.ErrorSync -> ErrorSyncFooter(viewModel)
        }
    }

    @Composable
    private fun CompleteFooter(viewModel: ConnectingViewModel) {
        GetLocalProperties { dimens, _, colors, _, types ->
            Column(modifier = Modifier.padding(dimens.contentPadding)) {
                Row {
                    Text(
                        text = stringResource(id = R.string.sync_state_title_sync_completed),
                        style = types.h1,
                    )
                    HSpacerSmall()
                    Image(
                        painter = painterResource(id = R.drawable.ic_connected),
                        contentDescription = null
                    )
                }
                VSpacerSmall()
                Text(
                    text = stringResource(id = R.string.sync_state_subtitle_sync_completed),
                    color = colors.shadeBlack0
                )
                VSpacerSmall()
            }
            DownButton(
                widgetModel = viewModel.completeButton,
                onClickAction = ConnectAction.Complete
            )
        }
    }

    @Composable
    private fun SyncFooter(glucometerName: String) {
        GetLocalProperties { dimens, _, colors, _, types ->
            Column(Modifier.padding(dimens.contentPadding)) {
                Row {
                    Text(
                        text = stringResource(id = R.string.sync_state_title_connected),
                        style = types.h1
                    )
                    HSpacerSmall()
                    Image(
                        painter = painterResource(id = R.drawable.ic_congrats),
                        contentDescription = null
                    )
                }
                VSpacerSmall()
                Text(
                    text = stringResource(id = R.string.sync_connected_text, glucometerName),
                    color = colors.shadeBlack0
                )
                VSpacerSmall()
            }
            BaseSnackBar(textId = R.string.sync_process_text)
        }
    }

    @Composable
    private fun ErrorSyncFooter(viewModel: ConnectingViewModel) {
        GetLocalProperties { dimens, _, colors, _, types ->
            Column(modifier = Modifier.padding(dimens.contentPadding)) {
                Text(
                    text = stringResource(id = R.string.sync_connection_connect_error_title),
                    style = types.h1
                )
                VSpacerSmall()
                Text(
                    text = stringResource(id = R.string.sync_connection_error_text),
                    color = colors.shadeBlack0
                )
                VSpacerSmall()
            }
            DownButton(
                widgetModel = viewModel.syncRepeatButton,
                onClickAction = ConnectAction.RepeatSync
            )
        }
    }

    @Composable
    private fun ErrorConnectFooter(viewModel: ConnectingViewModel) {
        GetLocalProperties { dimens, _, colors, _, types ->
            Column(modifier = Modifier.padding(dimens.contentPadding)) {
                Text(
                    text = stringResource(id = R.string.sync_connection_connect_error_title),
                    style = types.h1
                )
                VSpacerSmall()
                Text(
                    text = stringResource(id = R.string.sync_connection_error_text),
                    color = colors.shadeBlack0
                )
                VSpacerSmall()
            }
            DownButton(
                widgetModel = viewModel.connectRepeatButton,
                onClickAction = ConnectAction.RepeatConnect
            )
        }
    }

    @Composable
    private fun DeviceNotFoundFooter(viewModel: ConnectingViewModel) {
        GetLocalProperties { dimens, _, colors, _, types ->
            Column(modifier = Modifier.padding(dimens.contentPadding)) {
                Text(
                    text = stringResource(id = R.string.profile_device_search_not_found_title),
                    style = types.h1
                )
                VSpacerSmall()
                Text(
                    text = stringResource(id = R.string.profile_device_search_not_found_connect_scenario_text1),
                    color = colors.shadeBlack0
                )
                VSpacer(height = dimens.halfMediumDim)
                Text(
                    text = stringResource(id = R.string.profile_device_search_not_found_text2),
                    color = colors.shadeBlack0
                )
                VSpacerMedium()
                Text(
                    text = stringResource(id = R.string.profile_device_search_not_found_text3),
                    color = colors.shadeBlack0
                )
                VSpacerMedium()
                Text(
                    text = stringResource(id = R.string.profile_device_search_not_found_text4),
                    color = colors.shadeBlack0
                )
                VSpacerSmall()
            }
            DownButton(
                widgetModel = viewModel.searchRepeatButton,
                onClickAction = ConnectAction.RepeatSearch
            )
        }
    }

    @Composable
    private fun ConnectingFooter() {
        GetLocalProperties { dimens, _, colors, _, types ->
            Column(Modifier.padding(dimens.contentPadding)) {
                Text(
                    text = stringResource(id = R.string.sync_connection_screen_title),
                    style = types.h1
                )
                VSpacerSmall()
                Text(
                    text = stringResource(id = R.string.sync_connection_screen_text),
                    color = colors.shadeBlack0
                )
            }
            VSpacerSmall()
            BaseSnackBar(textId = R.string.connection_process_text)
        }
    }
}
