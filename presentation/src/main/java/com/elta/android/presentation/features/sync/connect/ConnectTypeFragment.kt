package com.elta.android.presentation.features.sync.connect

import androidx.compose.runtime.Composable
import androidx.fragment.app.viewModels
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.features.sync.connect.model.ConnectAction
import com.elta.android.presentation.features.sync.connect.viewmodel.ConnectTypeViewModel
import com.elta.android.presentation.utils.bundle

/** Keeps the existing entry route while it is replaced with the DMC introduction. */
class ConnectTypeFragment : BaseComposeFragment<ConnectTypeViewModel>() {
    companion object {
        fun newInstance(isOnBoarding: Boolean) = ConnectTypeFragment().apply {
            arguments = bundle(IS_ON_BOARDING_ARGUMENT_NAME to isOnBoarding)
        }
    }

    override val viewModel: ConnectTypeViewModel by viewModels { viewModelFactory }

    @Composable
    override fun Content(viewModel: ConnectTypeViewModel) {
        DmcConnectionIntroScreen(
            onBack = { viewModel sendAction AppAction.BackPressure },
            onScan = { viewModel sendAction ConnectAction.ConnectByDmc }
        )
    }
}
