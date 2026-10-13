package com.elta.android.presentation.features.sync.connect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.viewModels
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.features.sync.connect.model.ConnectAction
import com.elta.android.presentation.features.sync.connect.viewmodel.ConnectTypeViewModel
import com.elta.android.presentation.utils.bundle

open class ConnectTypeFragment : BaseComposeFragment<ConnectTypeViewModel>() {
    companion object {
        fun newInstance(isOnBoarding: Boolean) = ConnectTypeFragment().apply {
            arguments = bundle(IS_ON_BOARDING_ARGUMENT_NAME to isOnBoarding)
        }
    }

    override val viewModel: ConnectTypeViewModel by viewModels { viewModelFactory }
    override val padContentForNavigationBar = false

    @Composable
    override fun Content(viewModel: ConnectTypeViewModel) {
        val state by viewModel.state.collectAsState()
        ConnectionMethodScreen(
            isOnBoarding = state.isOnBoarding,
            onBack = { viewModel sendAction AppAction.BackPressure },
            onHelp = { viewModel sendAction ConnectAction.NeedHelp },
            onDmc = { viewModel sendAction ConnectAction.ConnectByDmc },
            onPin = { viewModel sendAction ConnectAction.ConnectByPin },
            onLater = { viewModel sendAction ConnectAction.SkipNextStep }
        )
    }
}
