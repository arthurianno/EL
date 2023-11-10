package com.elta.android.presentation.features.sync.connect

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.viewModels
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.core.compose.widgets.VSpacer
import com.elta.android.presentation.core.compose.widgets.VSpacerSmall
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBar
import com.elta.android.presentation.features.sync.connect.viewmodel.ConnectHelpViewModel
import com.elta.android.presentation.features.sync.connect.widgets.BluetoothString
import com.elta.android.presentation.features.sync.connect.widgets.TextNumericItem
import com.elta.android.presentation.theme.GetLocalProperties

class ConnectHelpFragment : BaseComposeFragment<ConnectHelpViewModel>() {
    override val viewModel: ConnectHelpViewModel by viewModels { viewModelFactory }

    override fun ConnectHelpViewModel.init() {
        appTopBar.setStartIconAction(AppAction.BackPressure)
    }

    @Composable
    override fun Content(viewModel: ConnectHelpViewModel) {
        GetLocalProperties { dimens, _, colors, _, _ ->
            Column(
                Modifier
                    .fillMaxSize()
                    .background(color = colors.white)
                    .systemBarsPadding()
            ) {
                AppTopBar(viewModel)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimens.contentPadding)
                        .verticalScroll(rememberScrollState())
                ) {
                    VSpacerSmall()
                    Header1()
                    VSpacerSmall()
                    TextBlock1()
                    VSpacer(height = dimens.connectHelpTextBlockInterval)
                    Header2()
                    VSpacer(dimens.halfMediumDim)
                    TextNumericItem(
                        number = R.string.list_numbering_1_dot,
                        text = R.string.connect_help_text_2_1
                    )
                    VSpacer(dimens.halfMediumDim)
                    TextNumericItem(
                        number = R.string.list_numbering_2_dot,
                        text = R.string.connect_help_text_2_2
                    )
                    VSpacer(dimens.halfMediumDim)
                    TextNumericItem(
                        number = R.string.list_numbering_3_dot,
                        text = R.string.connect_help_text_2_3
                    )
                    VSpacerSmall()
                    BluetoothString()
                    VSpacer(height = dimens.connectHelpTextBlockInterval)
                    Image(
                        painter = painterResource(id = R.drawable.img_connect_help),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    @Composable
    private fun Header2() {
        GetLocalProperties { _, _, _, _, types ->
            Text(
                text = stringResource(id = R.string.connect_help_header_2),
                style = types.h1
            )
        }
    }

    @Composable
    private fun TextBlock1() {
        GetLocalProperties { _, _, colors, _, _ ->
            Text(
                text = stringResource(id = R.string.connect_help_text_1_1),
                color = colors.shadeBlack0
            )
        }
    }

    @Composable
    private fun Header1() {
        GetLocalProperties { _, _, _, _, types ->
            Text(
                text = stringResource(id = R.string.connect_help_header_1),
                style = types.h1
            )
        }
    }

    @Composable
    private fun AppTopBar(viewModel: ConnectHelpViewModel) {
        BaseAppTopBar(
            widgetModel = viewModel.appTopBar,
            startIcon = R.drawable.ic_back
        )
    }
}
