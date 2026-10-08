package com.elta.android.presentation.features.registration.activation.ui

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.features.registration.activation.viewmodel.ActivationEffect
import com.elta.android.presentation.features.registration.activation.viewmodel.ActivationViewModel
import com.elta.android.presentation.messages.SnackBarMessageData
import com.elta.android.presentation.utils.makeSnackBar
import com.nullgr.core.ui.extensions.setStatusBarColor
import kotlinx.coroutines.launch

class ActivationFragment : BaseComposeFragment<ActivationViewModel>() {
    override val viewModel: ActivationViewModel by viewModels { viewModelFactory }
    override val padContentForNavigationBar = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effects.collect { effect ->
                    when (effect) {
                        is ActivationEffect.ShowMessage -> makeSnackBar(
                            requireActivity().findViewById(android.R.id.content),
                            SnackBarMessageData.SimpleTextMessage(effect.message)
                        ).show()
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        with(LightStatusBarConfigProvider) {
            requireActivity().window.setStatusBarColor(statusBarColor, lightStatusBar)
        }
    }

    @Composable
    override fun Content(viewModel: ActivationViewModel) {
        val state = viewModel.state.collectAsState()

        ActivationScreen(
            isLoading = state.value.isLoading,
            onBack = viewModel::backClick,
            onResend = viewModel::resend,
            onCheckActivation = viewModel::checkActivation
        )
    }

    companion object {
        fun newInstance(): ActivationFragment = ActivationFragment()
    }
}
