package com.elta.android.presentation.features.sync.connect

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.fragment.app.viewModels
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.core.compose.widgets.VSpacer
import com.elta.android.presentation.core.compose.widgets.VSpacerSmall
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBar
import com.elta.android.presentation.core.compose.widgets.buttons.SmallButton
import com.elta.android.presentation.features.sync.connect.model.ConnectAction
import com.elta.android.presentation.features.sync.connect.viewmodel.ConnectTypeViewModel
import com.elta.android.presentation.features.sync.connect.widgets.MainImage
import com.elta.android.presentation.theme.GetLocalProperties
import com.elta.android.presentation.utils.bundle

class ConnectTypeFragment : BaseComposeFragment<ConnectTypeViewModel>() {
    companion object {
        fun newInstance(isOnBoarding: Boolean): ConnectTypeFragment =
            ConnectTypeFragment().apply {
                arguments = bundle(IS_ON_BOARDING_ARGUMENT_NAME to isOnBoarding)
            }
    }

    override val viewModel: ConnectTypeViewModel by viewModels { viewModelFactory }

    override fun ConnectTypeViewModel.init() {
        appTopBar.setStartIconAction(AppAction.BackPressure)
        appTopBar.setEndIconAction(ConnectAction.NeedHelp)
    }

    @Composable
    override fun Content(viewModel: ConnectTypeViewModel) {
        GetLocalProperties { dimens, _, colors, _, _ ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = colors.white)
                    .systemBarsPadding()
            ) {
                TopAppBar(viewModel)
                MainImage(imageId = R.drawable.ic_connect_dev)
                VSpacer(dimens.connectTypeImageToTextVInterval)
                Footer(viewModel)
            }
        }
    }

    @Composable
    private fun Footer(viewModel: ConnectTypeViewModel) {
        GetLocalProperties { dimens, _, _, _, _ ->
            Column(Modifier.padding(dimens.contentPadding)) {
                Info()
                VSpacer(dimens.bigDim)
                Buttons(viewModel)
                VSpacerSmall()
            }
        }
    }

    @Composable
    private fun Info() {
        GetLocalProperties { _, _, colors, _, types ->
            Text(
                text = stringResource(id = R.string.sync_connect_type_title),
                style = types.h1,
                color = colors.blackBlue,
                modifier = Modifier.fillMaxWidth()
            )
            VSpacerSmall()
            Text(
                text = stringResource(id = R.string.sync_connect_type_text),
                style = types.body1,
                color = colors.shadeBlack0,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    @Composable
    private fun Buttons(viewModel: ConnectTypeViewModel) {
        GetLocalProperties { dimens, _, colors, _, types ->
            SmallButton(
                text = stringResource(id = R.string.sync_connect_type_button_enter_pin),
                icon = R.drawable.ic_pin_icon,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimens.connectTypeButtonsHeight),
                onClick = {
                    viewModel sendAction ConnectAction.ConnectByPin
                }
            )
            VSpacerSmall()
            Text(
                text = stringResource(id = R.string.sync_connect_type_text_or),
                style = types.body1,
                color = colors.shadeBlack0,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            VSpacerSmall()
            SmallButton(
                text = stringResource(id = R.string.sync_connect_type_button_scan_dmc),
                icon = R.drawable.ic_dmc_scan_icon,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimens.connectTypeButtonsHeight),
                onClick = {
                    viewModel sendAction ConnectAction.ConnectByDmc
                }
            )
        }
    }

    @Composable
    private fun TopAppBar(viewModel: ConnectTypeViewModel) {
        GetLocalProperties { _, _, colors, _, _ ->
            BaseAppTopBar(
                widgetModel = viewModel.appTopBar,
                startIcon = R.drawable.ic_back,
                startIconColor = colors.blackBlue,
                endText = R.string.sync_connect_type_button_need_help
            )
        }
    }
}
