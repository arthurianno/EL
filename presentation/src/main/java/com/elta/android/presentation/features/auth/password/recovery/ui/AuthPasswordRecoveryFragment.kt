package com.elta.android.presentation.features.auth.password.recovery.ui

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.core.navigation.FlowRouter
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.features.auth.password.recovery.model.PasswordRecoveryEffect
import com.elta.android.presentation.features.auth.password.recovery.viewmodel.PasswordRecoveryViewModel
import com.elta.android.presentation.messages.SnackBarMessageData
import com.elta.android.presentation.utils.hideKeyboardFun
import com.elta.android.presentation.utils.makeSnackBar
import com.nullgr.core.ui.extensions.setStatusBarColor
import kotlinx.coroutines.launch

class AuthPasswordRecoveryFragment : BaseComposeFragment<PasswordRecoveryViewModel>() {
    override val viewModel: PasswordRecoveryViewModel by viewModels { viewModelFactory }

    override fun PasswordRecoveryViewModel.init() {
        initialize(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effects.collect { effect ->
                    view.hideKeyboardFun()
                    when (effect) {
                        is PasswordRecoveryEffect.LinkSent -> {
                            showMessage(getString(R.string.registration_email_sent))
                            viewModel.router.navigateTo(
                                if (effect.useNewLogin) Screens.Login else Screens.LoginVariantA
                            )
                            viewModel.onLinkSentHandled()
                        }
                        is PasswordRecoveryEffect.ShowMessage ->
                            showMessage(effect.text ?: getString(effect.resource))
                        PasswordRecoveryEffect.AuthenticationRequired ->
                            (viewModel.router as FlowRouter).newRootFlow(Screens.AuthFlow)
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

    override fun onPause() {
        view?.hideKeyboardFun()
        super.onPause()
    }

    @Composable
    override fun Content(viewModel: PasswordRecoveryViewModel) {
        val lifecycle = viewLifecycleOwner.lifecycle
        val state = remember(viewModel, lifecycle) {
            viewModel.state.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
        }.collectAsState(initial = viewModel.state.value)
        PasswordRecoveryScreen(
            state = state.value,
            onAction = viewModel.actionReceiver,
            onClose = viewModel::backClick
        )
    }

    private fun showMessage(message: String) {
        makeSnackBar(
            requireActivity().findViewById(android.R.id.content),
            SnackBarMessageData.SimpleTextMessage(message)
        ).show()
    }

    companion object {
        fun newInstance() = AuthPasswordRecoveryFragment()
    }
}
