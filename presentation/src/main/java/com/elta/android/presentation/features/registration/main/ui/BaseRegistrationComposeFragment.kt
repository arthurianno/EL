package com.elta.android.presentation.features.registration.main.ui

import android.os.Bundle
import android.view.View
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.features.registration.main.model.RegistrationMode
import com.elta.android.presentation.features.registration.main.viewmodel.RegistrationEffect
import com.elta.android.presentation.features.registration.main.viewmodel.RegistrationViewModel
import com.elta.android.presentation.features.registration.policy.ui.RegistrationPrivacyPolicyFragment
import com.elta.android.presentation.messages.SnackBarMessageData
import com.elta.android.presentation.utils.hideKeyboardFun
import com.elta.android.presentation.utils.makeSnackBar
import com.nullgr.core.ui.extensions.setStatusBarColor
import com.nullgr.core.ui.fragments.showDialog
import kotlinx.coroutines.launch

abstract class BaseRegistrationComposeFragment : BaseComposeFragment<RegistrationViewModel>() {
    override val viewModel: RegistrationViewModel by viewModels { viewModelFactory }
    override val padContentForNavigationBar = false

    protected abstract val registrationMode: RegistrationMode

    override fun RegistrationViewModel.init() {
        initialize(registrationMode)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effects.collect { effect ->
                    when (effect) {
                        is RegistrationEffect.ShowDocument -> {
                            view.hideKeyboardFun()
                            childFragmentManager.showDialog(
                                RegistrationPrivacyPolicyFragment.newInstance(getString(effect.urlResource))
                            )
                        }
                        is RegistrationEffect.ShowMessage -> {
                            view.hideKeyboardFun()
                            makeSnackBar(
                                requireActivity().findViewById(android.R.id.content),
                                SnackBarMessageData.SimpleTextMessage(effect.message)
                            ).show()
                        }
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
    override fun Content(viewModel: RegistrationViewModel) {
        val lifecycle = viewLifecycleOwner.lifecycle
        val state = remember(viewModel, lifecycle) {
            viewModel.state.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
        }.collectAsState(initial = viewModel.state.value)

        RegistrationScreen(
            state = state.value,
            onAction = viewModel::onAction,
            onBack = viewModel::backClick,
            onLogin = viewModel::loginClick
        )
        if (state.value.dialog != null) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text(stringResource(R.string.profile_is_deleted_title_variant_a)) },
                text = { Text(stringResource(R.string.profile_is_deleted_body_variant_a)) },
                confirmButton = {
                    TextButton(onClick = viewModel::dismissDialog) {
                        Text(stringResource(R.string.ok))
                    }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::contactSupport) {
                        Text(stringResource(R.string.profile_support_email_description))
                    }
                }
            )
        }
    }
}
