package com.elta.android.presentation.features.sync.connect

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.viewModels
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.core.compose.widgets.VSpacer
import com.elta.android.presentation.core.compose.widgets.VSpacerSmall
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBar
import com.elta.android.presentation.core.compose.widgets.buttons.DownButton
import com.elta.android.presentation.features.sync.connect.model.ConnectAction
import com.elta.android.presentation.features.sync.connect.viewmodel.ConnectStartViewModel
import com.elta.android.presentation.features.sync.connect.widgets.MainImage
import com.elta.android.presentation.theme.GetLocalProperties
import com.elta.android.presentation.utils.bundle

internal const val IS_ON_BOARDING_ARGUMENT_NAME = "is_onBoarding"
internal const val PIN_ARGUMENT_NAME = "pin"
internal const val GLUCOMETER_NAME_ARGUMENT_NAME = "glucometer_name"

class ConnectStartFragment : BaseComposeFragment<ConnectStartViewModel>() {

    companion object {
        fun newInstance(isOnBoarding: Boolean): ConnectStartFragment =
            ConnectStartFragment().apply {
                arguments = bundle(IS_ON_BOARDING_ARGUMENT_NAME to isOnBoarding)
            }
    }

    override val viewModel: ConnectStartViewModel by viewModels { viewModelFactory }

    override fun ConnectStartViewModel.init() {
        appTopBar.setEndIconAction(ConnectAction.SkipNextStep)
        appTopBar.setStartIconAction(AppAction.BackPressure)

        downButton.setText(getString(R.string.sync_state_pin_dialog_button))
    }

    @Composable
    override fun Content(viewModel: ConnectStartViewModel) {
        GetLocalProperties { dimens, _, colors, _, _ ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = colors.white)
                    .systemBarsPadding()
            ) {
                TopAppBar(viewModel)
                MainImage(imageId = R.drawable.ic_connect_dev)
                Title()
                VSpacerSmall()
                Body()
                VSpacer(height = dimens.bigDim)
                DownButton(widgetModel = viewModel.downButton)
            }
        }
    }

    @Composable
    private fun Body() {
        GetLocalProperties { dimens, _, colors, _, _ ->
            Text(
                text = stringResource(id = R.string.sync_connect_start_text),
                color = colors.shadeBlack0,
                modifier = Modifier.padding(horizontal = dimens.contentPadding)
            )
        }
    }

    @Composable
    private fun Title() {
        GetLocalProperties { dimens, _, _, _, types ->
            Text(
                text = stringResource(id = R.string.sync_connect_start_title),
                style = types.h1,
                modifier = Modifier.padding(horizontal = dimens.contentPadding)
            )
        }
    }

    @Composable
    fun TopAppBar(viewModel: ConnectStartViewModel) {
        val state = viewModel.state.collectAsState()
        val isOnboarding = state.value.isOnBoarding
        
        val endTextId = if (isOnboarding) R.string.sync_start_menu_button_text else null
        val startIconId = if (!isOnboarding) R.drawable.ic_dialog_close else null

        GetLocalProperties { _, _, colors, _, _ ->
            BaseAppTopBar(
                widgetModel = viewModel.appTopBar,
                backgroundColor = colors.white,
                startIcon = startIconId,
                startIconColor = colors.blackBlue,
                endText = endTextId
            )
        }
    }
}

@Preview
@Composable
private fun PreviewContent() {
    val viewModel = ConnectStartViewModel().apply {
        downButton.setText(stringResource(id = R.string.sync_connect_start_title))
    }
    ConnectStartFragment().Content(viewModel = viewModel)
}
