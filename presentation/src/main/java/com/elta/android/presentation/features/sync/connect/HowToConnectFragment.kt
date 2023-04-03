package com.elta.android.presentation.features.sync.connect

import android.Manifest
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.viewModels
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.core.compose.common.PermissionEvent
import com.elta.android.presentation.core.compose.widgets.HSpacerSmall
import com.elta.android.presentation.core.compose.widgets.VSpacer
import com.elta.android.presentation.core.compose.widgets.VSpacerMedium
import com.elta.android.presentation.core.compose.widgets.VSpacerSmall
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBar
import com.elta.android.presentation.core.compose.widgets.buttons.DownButton
import com.elta.android.presentation.features.sync.connect.model.ConnectAction
import com.elta.android.presentation.features.sync.connect.viewmodel.HowToConnectViewModel
import com.elta.android.presentation.features.sync.connect.widgets.BluetoothString
import com.elta.android.presentation.features.sync.connect.widgets.MainImage
import com.elta.android.presentation.features.sync.connect.widgets.TextNumericItem
import com.elta.android.presentation.theme.GetLocalProperties
import com.elta.android.presentation.utils.bundle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState

private val requiredPermissions = listOf(
    Manifest.permission.CAMERA,
    Manifest.permission.ACCESS_FINE_LOCATION
)

class HowToConnectFragment : BaseComposeFragment<HowToConnectViewModel>() {
    companion object {
        fun newInstance(isOnBoarding: Boolean) = HowToConnectFragment().apply {
            arguments = bundle(IS_ON_BOARDING_ARGUMENT_NAME to isOnBoarding)
        }
    }

    override val viewModel: HowToConnectViewModel by viewModels { viewModelFactory }

    override fun HowToConnectViewModel.init() {
        appTopBar.setStartIconAction(AppAction.BackPressure)
        downButton.setText(getString(R.string.sync_how_to_connect_button))
        downButton.setEnableState(true)
        downButton.visibilityState(true)
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    override fun Content(viewModel: HowToConnectViewModel) {
        val cameraPermission =
            rememberMultiplePermissionsState(permissions = requiredPermissions)
        val event = viewModel.event.collectAsState(initial = null).value
        LaunchedEffect(key1 = event) {
            if (event is PermissionEvent.Camera ||
                event is PermissionEvent.FineLocation
            ) {
                cameraPermission.launchMultiplePermissionRequest()
            }
        }
        GetLocalProperties { dimens, _, _, _, _ ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
            ) {
                TopAppBar()
                MainImage(imageId = R.drawable.img_dmc_connect)
                Info()
                VSpacer(dimens.bigDim)
                ScanButton(viewModel, cameraPermission)
            }
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    private fun ScanButton(
        viewModel: HowToConnectViewModel,
        cameraPermission: MultiplePermissionsState
    ) {
        Box {
            DownButton(
                widgetModel = viewModel.downButton,
                onClickAction = ConnectAction.OpenConnectingScreen(cameraPermission.permissions)
            )
        }
    }

    @Composable
    private fun Info() {
        GetLocalProperties { dimens, _, _, _, _ ->
            Column(Modifier.padding(dimens.contentPadding)) {
                Title()
                VSpacer(dimens.halfMediumDim)
                TextNumericItem(
                    number = R.string.list_numbering_1_dot,
                    text = R.string.sync_how_to_connect_text_1
                )
                VSpacerSmall()
                BluetoothString()
                VSpacerMedium()
                TextNumericItem(
                    number = R.string.list_numbering_2_dot,
                    text = R.string.sync_how_to_connect_text_2
                )
            }
        }
    }

    @Composable
    private fun Title() {
        GetLocalProperties { _, _, colors, _, types ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(id = R.string.sync_how_to_connect_title),
                    style = types.h1
                )
                HSpacerSmall()
                Image(
                    painter = painterResource(id = R.drawable.ic_how_to_connect),
                    contentDescription = null
                )
            }
        }
    }

    @Composable
    private fun TopAppBar() {
        GetLocalProperties { _, _, colors, _, _ ->
            BaseAppTopBar(
                widgetModel = viewModel.appTopBar,
                backgroundColor = colors.white,
                startIcon = R.drawable.ic_back,
                startIconColor = colors.blackBlue
            )
        }
    }
}
