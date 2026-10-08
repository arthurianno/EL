package com.elta.android.presentation.features.auth.login.ui

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
import com.elta.android.presentation.features.auth.login.model.LoginDialog
import com.elta.android.presentation.features.auth.login.model.LoginMode
import com.elta.android.presentation.features.auth.login.viewmodel.LoginEffect
import com.elta.android.presentation.features.auth.login.viewmodel.LoginViewModel
import com.elta.android.presentation.messages.SnackBarMessageData
import com.elta.android.presentation.utils.hideKeyboardFun
import com.elta.android.presentation.utils.makeSnackBar
import com.nullgr.core.ui.extensions.setStatusBarColor
import kotlinx.coroutines.launch

abstract class BaseLoginComposeFragment : BaseComposeFragment<LoginViewModel>() {
    override val viewModel: LoginViewModel by viewModels { viewModelFactory }
    override val padContentForNavigationBar = false

    protected abstract val loginMode: LoginMode

    override fun LoginViewModel.init() {
        initialize(loginMode)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effects.collect { effect ->
                    when (effect) {
                        is LoginEffect.ShowMessage -> {
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
    override fun Content(viewModel: LoginViewModel) {
        val lifecycle = viewLifecycleOwner.lifecycle
        val state = remember(viewModel, lifecycle) {
            viewModel.state.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
        }.collectAsState(initial = viewModel.state.value)

        LoginScreen(
            state = state.value,
            onAction = viewModel::onAction,
            onBack = viewModel::backClick,
            onForgotPassword = viewModel::forgotPassword
        )
        state.value.dialog?.let { dialog ->
            LoginDialogContent(
                dialog = dialog,
                mode = loginMode,
                onPositive = viewModel::onDialogPositive,
                onNegative = viewModel::onDialogNegative
            )
        }
    }
}

@Composable
private fun LoginDialogContent(
    dialog: LoginDialog,
    mode: LoginMode,
    onPositive: () -> Unit,
    onNegative: () -> Unit
) {
    val title = when {
        dialog == LoginDialog.ProfileRestored -> null
        mode == LoginMode.Legacy -> stringResource(R.string.profile_is_deleted_title_variant_a)
        else -> stringResource(R.string.profile_is_deleted_title)
    }
    val message = when {
        dialog == LoginDialog.ProfileRestored -> stringResource(R.string.profile_is_deleted_restored_body)
        mode == LoginMode.Legacy -> stringResource(R.string.profile_is_deleted_body_variant_a)
        else -> stringResource(R.string.profile_is_deleted_body)
    }
    val positive = when {
        dialog == LoginDialog.ProfileRestored || mode == LoginMode.Legacy -> stringResource(R.string.ok)
        else -> stringResource(R.string.profile_is_deleted_restore_button)
    }
    val negative = when {
        dialog == LoginDialog.ProfileRestored -> null
        mode == LoginMode.Legacy -> stringResource(R.string.profile_support_email_description)
        else -> stringResource(R.string.cancel_text)
    }

    AlertDialog(
        onDismissRequest = {},
        title = title?.let { { Text(it) } },
        text = { Text(message) },
        confirmButton = { TextButton(onClick = onPositive) { Text(positive) } },
        dismissButton = negative?.let { label ->
            { TextButton(onClick = onNegative) { Text(label) } }
        }
    )
}
