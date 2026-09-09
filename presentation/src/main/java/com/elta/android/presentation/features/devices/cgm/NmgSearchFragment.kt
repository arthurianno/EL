package com.elta.android.presentation.features.devices.cgm

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.core.compose.widgets.VSpacerSmall
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBar
import com.elta.android.presentation.theme.GetLocalProperties

class NmgSearchFragment : BaseComposeFragment<NmgSearchViewModel>() {
    override val viewModel: NmgSearchViewModel by viewModels { viewModelFactory }

    override fun NmgSearchViewModel.init() {
        appBar.setStartIconAction(AppAction.BackPressure)
    }

    @Composable
    override fun Content(viewModel: NmgSearchViewModel) {
        val state = viewModel.state.collectAsState().value
        val context = requireContext()
        LaunchedEffect(Unit) {
            val notificationsGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
            viewModel sendAction NmgSearchAction.NotificationPermissionResult(notificationsGranted)
            if (scanPermissions().all { permission ->
                    ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
                }
            ) {
                viewModel sendAction NmgSearchAction.StartScan
            } else {
                permissionsLauncher.launch(scanPermissions())
            }
        }

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
                Column(Modifier.padding(dimens.contentPadding)) {
                    Text(
                        text = stringResource(R.string.nmg_search_title),
                        style = types.h1,
                        color = colors.blackBlue
                    )
                    VSpacerSmall()
                    Text(
                        text = stringResource(R.string.nmg_search_description),
                        style = types.body1,
                        color = colors.shadeBlack0
                    )
                    if (state.notificationsUnavailable) {
                        VSpacerSmall()
                        Text(
                            text = stringResource(R.string.nmg_search_notifications_disabled),
                            style = types.body1,
                            color = colors.red
                        )
                    }
                    VSpacerSmall()
                    when {
                        state.hasError -> Text(
                            text = stringResource(R.string.nmg_search_error),
                            style = types.body1,
                            color = colors.red
                        )
                        state.sensors.isEmpty() -> Text(
                            text = stringResource(
                                if (state.isScanning) R.string.nmg_search_in_progress
                                else R.string.nmg_search_waiting
                            ),
                            style = types.body1,
                            color = colors.shadeBlack0
                        )
                    }
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(state.sensors, key = { sensor -> sensor.id }) { sensor ->
                            VSpacerSmall()
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { viewModel sendAction NmgSearchAction.SelectSensor(sensor.id) }
                            ) {
                                Text("${sensor.name} ${sensor.id}")
                            }
                        }
                    }
                }
            }
        }
    }

    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val scanPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            result[Manifest.permission.BLUETOOTH_SCAN] == true
        } else {
            result[Manifest.permission.ACCESS_FINE_LOCATION] == true
        }
        if (scanPermission) {
            viewModel sendAction NmgSearchAction.NotificationPermissionResult(
                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    result[Manifest.permission.POST_NOTIFICATIONS] == true
            )
            viewModel sendAction NmgSearchAction.StartScan
        }
    }

    private fun scanPermissions(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
}
