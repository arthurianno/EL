package com.elta.android.presentation.features.devices.search

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.viewModels
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.core.compose.widgets.HSpacerVerySmall
import com.elta.android.presentation.core.compose.widgets.VSpacer
import com.elta.android.presentation.core.compose.widgets.VSpacerMedium
import com.elta.android.presentation.core.compose.widgets.VSpacerSmall
import com.elta.android.presentation.core.compose.widgets.animation.VerticallyAnimation
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBar
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBarWidgetModel
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialog
import com.elta.android.presentation.core.compose.widgets.snackbar.BaseSnackBar
import com.elta.android.presentation.core.ui.fragment.addOnBackPressedCallback
import com.elta.android.presentation.features.devices.search.model.GlucometerSearchStatus
import com.elta.android.presentation.features.devices.search.viewmodel.GlucometerSearchViewModel
import com.elta.android.presentation.features.devices.search.widgets.GlucometerSearchButton
import com.elta.android.presentation.theme.GetLocalProperties
import com.elta.android.presentation.utils.bundle

internal const val ADDRESS_ARGUMENT_ID = "address_argument"

class GlucometerSearchFragment : BaseComposeFragment<GlucometerSearchViewModel>() {

    companion object {
        fun newInstance(address: String): GlucometerSearchFragment =
            GlucometerSearchFragment().apply {
                arguments = bundle(ADDRESS_ARGUMENT_ID to address)
            }
    }

    override val viewModel: GlucometerSearchViewModel by viewModels { viewModelFactory }

    override fun GlucometerSearchViewModel.init() {
        appBar.setStartIconAction(AppAction.BackPressure)
        cancelSearchDialog.initDialog(
            message = getString(R.string.profile_device_cancel_search_dialog_text),
            positiveButtonText = getString(R.string.yes_text),
            negativeButtonText = getString(R.string.event_form_dialog_cancel_button)
        )
        cancelRingDialog.initDialog(
            message = getString(R.string.profile_device_cancel_ring_dialog_text),
            positiveButtonText = getString(R.string.yes_text),
            negativeButtonText = getString(R.string.event_form_dialog_cancel_button)
        )
    }

    @Composable
    override fun Dialogs(viewModel: GlucometerSearchViewModel) {
        BaseDialog(widgetModel = viewModel.cancelSearchDialog)
        BaseDialog(widgetModel = viewModel.cancelRingDialog)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        addOnBackPressedCallback { viewModel.backClick() }
    }

    @Composable
    override fun Content(viewModel: GlucometerSearchViewModel) {
        GetLocalProperties { dimens, _, _, _, _ ->
            val state = viewModel.state.collectAsState().value
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AppBar(viewModel.appBar)
                        VSpacer(height = dimens.searchButtonTopInterval)
                        GlucometerSearchButton(widgetModel = viewModel.searchButton)
                    }
                    when (state.searchStatus) {
                        GlucometerSearchStatus.Off -> SearchOffText()
                        GlucometerSearchStatus.On -> SearchOnText()
                        GlucometerSearchStatus.Connecting -> ConnectionText()
                        GlucometerSearchStatus.DeviceNotFound -> DeviceNotFound()
                    }
                }
                VerticallyAnimation(visualState = state.searchStatus == GlucometerSearchStatus.Connecting) {
                    BaseSnackBar(state.snackBar.stringId)
                }
            }
        }
    }

    @Composable
    private fun BoxScope.DeviceNotFound() {
        GetLocalProperties { dimens, _, colors, _, types ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimens.searchDeviceBottomTextPadding)
                    .align(Alignment.BottomCenter)
            ) {
                Text(
                    text = stringResource(id = R.string.profile_device_search_not_found_title),
                    style = types.h1
                )
                VSpacer(height = dimens.halfMediumDim)
                Text(
                    text = stringResource(id = R.string.profile_device_search_not_found_text1),
                    color = colors.shadeBlack0
                )
                VSpacer(height = dimens.halfMediumDim)
                Text(
                    text = stringResource(id = R.string.profile_device_search_not_found_disable_ble),
                    color = colors.shadeBlack0
                )
                VSpacerMedium()
                Text(
                    text = stringResource(id = R.string.profile_device_search_not_found_low_energy),
                    color = colors.shadeBlack0
                )
                VSpacerMedium()
                Text(
                    text = stringResource(id = R.string.profile_device_search_not_found_out_of_range),
                    color = colors.shadeBlack0
                )
            }
        }
    }

    @Composable
    private fun BoxScope.SearchOnText() {
        GetLocalProperties { dimens, _, colors, _, types ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimens.searchDeviceBottomTextPadding)
                    .align(Alignment.BottomCenter)
            ) {
                Row {
                    Text(
                        text = stringResource(id = R.string.profile_device_search_on_title),
                        style = types.h1
                    )
                    HSpacerVerySmall()
                    Image(
                        painter = painterResource(id = R.drawable.ic_dinamic_on),
                        contentDescription = null,
                        modifier = Modifier.size(dimens.bigDim)
                    )
                }
                VSpacerSmall()
                Text(
                    text = stringResource(id = R.string.profile_device_search_on_text1),
                    color = colors.shadeBlack0
                )
                VSpacerSmall()
                Text(
                    text = stringResource(id = R.string.profile_device_search_on_text2),
                    color = colors.shadeBlack0
                )
            }
        }
    }

    @Composable
    private fun BoxScope.SearchOffText() {
        GetLocalProperties { dimens, _, colors, _, types ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimens.searchDeviceBottomTextPadding)
                    .align(Alignment.BottomCenter)
            ) {
                Row {
                    Text(
                        text = stringResource(id = R.string.profile_device_search_off_title),
                        style = types.h1
                    )
                    HSpacerVerySmall()
                    Image(
                        painter = painterResource(id = R.drawable.ic_dinamic_off),
                        contentDescription = null,
                        modifier = Modifier.size(dimens.bigDim)
                    )
                }
                VSpacerSmall()
                Text(
                    text = stringResource(id = R.string.profile_device_search_off_text),
                    color = colors.shadeBlack0
                )
            }
        }
    }

    @Composable
    private fun BoxScope.ConnectionText() {
        GetLocalProperties { dimens, _, colors, _, types ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimens.searchDeviceBottomTextPadding)
                    .align(Alignment.BottomCenter)
            ) {
                Text(
                    text = stringResource(id = R.string.profile_device_search_connecting_title),
                    style = types.h1
                )
                VSpacerSmall()
                Text(
                    text = stringResource(id = R.string.profile_device_search_connecting_text),
                    color = colors.shadeBlack0
                )
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
}
