package com.elta.android.presentation.features.devices.cgm

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.viewModels
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.core.compose.widgets.VSpacerSmall
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBar
import com.elta.android.presentation.theme.GetLocalProperties

class NmgMonitoringFragment : BaseComposeFragment<NmgMonitoringViewModel>() {
    override val viewModel: NmgMonitoringViewModel by viewModels { viewModelFactory }

    override fun NmgMonitoringViewModel.init() {
        appBar.setStartIconAction(AppAction.BackPressure)
    }

    @Composable
    override fun Content(viewModel: NmgMonitoringViewModel) {
        val state = viewModel.state.collectAsState().value
        GetLocalProperties { dimens, _, colors, _, types ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.white)
                    .statusBarsPadding()
            ) {
                BaseAppTopBar(
                    widgetModel = viewModel.appBar,
                    startIcon = R.drawable.ic_back,
                    startIconColor = colors.blackBlue
                )
                Column(
                    Modifier
                        .padding(dimens.contentPadding)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        stringResource(R.string.nmg_monitoring_title),
                        style = types.h1,
                        color = colors.blackBlue
                    )
                    VSpacerSmall()
                    Text(
                        text = state.sensorId ?: stringResource(R.string.nmg_monitoring_no_sensor),
                        style = types.body1,
                        color = colors.shadeBlack0
                    )
                    VSpacerSmall()
                    val measurement = state.lastMeasurement
                    if (measurement == null) {
                        Text(
                            stringResource(R.string.nmg_monitoring_waiting),
                            style = types.body1,
                            color = colors.shadeBlack0
                        )
                    } else {
                        Text(
                            text = stringResource(
                                R.string.nmg_monitoring_signal,
                                measurement.glucoseSignalNanoAmp
                            ),
                            style = types.h1,
                            color = colors.blackBlue
                        )
                        VSpacerSmall()
                        Text(
                            text = stringResource(
                                R.string.nmg_monitoring_history,
                                state.historySize
                            ),
                            style = types.body1,
                            color = colors.shadeBlack0
                        )
                    }
                    VSpacerSmall()
                    Text(
                        stringResource(R.string.nmg_monitoring_test_data_note),
                        style = types.body1,
                        color = colors.shadeBlack0
                    )
                    VSpacerSmall()
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { viewModel sendAction NmgMonitoringAction.StopMonitoring }
                    ) {
                        Text(stringResource(R.string.nmg_monitoring_stop))
                    }
                }
            }
        }
    }
}
