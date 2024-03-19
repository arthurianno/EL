package com.elta.android.presentation.features.version.mandatory.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.core.compose.widgets.buttons.DownButton
import com.elta.android.presentation.features.version.mandatory.model.MandatoryUpdateEvent
import com.elta.android.presentation.features.version.mandatory.viewmodel.MandatoryUpdateViewModel
import com.elta.android.presentation.features.version.openAppInStoreIntent
import com.elta.android.presentation.theme.GetLocalProperties

class MandatoryUpdateFragment : BaseComposeFragment<MandatoryUpdateViewModel>() {
    companion object {
        fun newInstance() = MandatoryUpdateFragment()
    }

    override val viewModel: MandatoryUpdateViewModel by viewModels { viewModelFactory }

    override fun MandatoryUpdateViewModel.init() {
        downButton.setText(getString(R.string.update_app_button))
    }

    @Composable
    override fun Content(viewModel: MandatoryUpdateViewModel) {
        val context = LocalContext.current
        val event = viewModel.event.collectAsState(initial = null).value

        if (event == MandatoryUpdateEvent.OpenAppPageInStore)
            context.openAppInStoreIntent(context.packageName)

        MandatoryUpdateContent(viewModel)
    }

    @Composable
    fun MandatoryUpdateContent(viewModel: MandatoryUpdateViewModel) {
        GetLocalProperties { dimens, _, colors, _, styles ->
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                scaffoldState = rememberScaffoldState(),
                bottomBar = { DownButton(widgetModel = viewModel.downButton) }
            ) { paddingValues ->

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(dimens.contentListPadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        modifier = Modifier.padding(dimens.imageForcedUpdatePadding),
                        painter = painterResource(id = R.drawable.ic_welcome),
                        alignment = Alignment.Center,
                        contentDescription = null
                    )
                    Text(
                        modifier = Modifier.padding(dimens.titleForcedUpdatePadding),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        text = stringResource(R.string.update_forced_app_is_old_title),
                        style = styles.h1
                    )
                    Text(
                        modifier = Modifier.padding(horizontal = dimens.contentPadding),
                        text = stringResource(R.string.update_forced_app_is_not_supported_description),
                        style = styles.body1,
                        color = colors.shadeBlack0,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    @Preview
    @Composable
    fun PreviewMandatoryUpdateContent() {
        Box {
            MandatoryUpdateContent(viewModel = viewModel())
        }
    }
}
