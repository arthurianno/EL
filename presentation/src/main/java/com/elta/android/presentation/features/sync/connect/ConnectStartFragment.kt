package com.elta.android.presentation.features.sync.connect

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    }

    @Composable
    override fun Content(viewModel: ConnectStartViewModel) {
        val state = viewModel.state.collectAsState()
        var expanded by remember { mutableStateOf(false) }
        //val items = listOf("Satellite Online", "Satellite Monitor", "Test 1", "Test 2")
        //var selectedItem by remember { mutableStateOf(items.first()) }
        GetLocalProperties { dimens, _, colors, _, _ ->

            AnimatedVisibility(
                visible = viewModel.state.collectAsState().value.isContentReady,
                enter = fadeIn()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = colors.white)
                        .statusBarsPadding()
                ) {
                    TopAppBar(viewModel)
                    MainImage(
                        imageUrl = state.value.screenConfig?.backgroundImageUrl,
                        imageId = R.drawable.ic_connect_dev
                    )
                    Title(
                        state.value.screenConfig?.title
                            ?: stringResource(id = R.string.sync_connect_start_title)
                    )
//                        OutlinedButton(
//                            onClick = { expanded = true },
//                            modifier = Modifier.fillMaxWidth(),
//                            colors = ButtonDefaults.outlinedButtonColors(
//                                backgroundColor = colors.white
//                            )
//                        ) {
//                            Text(
//                                text = selectedItem,
//                                modifier = Modifier.weight(1f),
//                                color = colors.blackBlue
//                            )
//                            Text(
//                                text = "▼",
//                                color = colors.blackBlue
//                            )
//                        }
//
//                        DropdownMenu(
//                            expanded = expanded,
//                            onDismissRequest = { expanded = false },
//                            modifier = Modifier.fillMaxWidth()
//                        ) {
//                            items.forEach { item ->
//                                DropdownMenuItem(
//                                    onClick = {
//                                        selectedItem = item
//                                        expanded = false
//                                    }
//                                ) {
//                                    Text(text = item)
//                                }
//                            }
//                        }
//                    }
//                    VSpacerSmall()
                    Body(
                        state.value.screenConfig?.description
                            ?: stringResource(id = R.string.sync_connect_start_text)
                    )
                    VSpacer(height = dimens.bigDim)
                    DownButton(widgetModel = viewModel.downButton)
                }
            }
        }
    }

    @Composable
    private fun Body(body : String) {
        GetLocalProperties { dimens, _, colors, _, types ->
            Text(
                text = body,
                style = types.body1,
                color = colors.shadeBlack0,
                modifier = Modifier.padding(horizontal = dimens.contentPadding)
            )
        }
    }

    @Composable
    private fun Title(title : String) {
        GetLocalProperties { dimens, _, colors, _, types ->
            Text(
                text = title,
                style = types.h1,
                color = colors.blackBlue,
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
