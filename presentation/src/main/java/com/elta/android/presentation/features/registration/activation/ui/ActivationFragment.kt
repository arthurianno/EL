package com.elta.android.presentation.features.registration.activation.ui

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentActivateProfileBinding
import com.elta.android.presentation.features.registration.activation.pm.ActivationPm
import com.elta.android.presentation.theme.EltaTheme
import me.dmdev.rxpm.bindTo

class ActivationFragment :
    BaseFragment<ActivationPm, FragmentActivateProfileBinding>(FragmentActivateProfileBinding::inflate) {

    override val screenLayout: Int = R.layout.fragment_activate_profile
    override val classToken: Class<ActivationPm> = ActivationPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider
    override val applyBottomSystemInsets = false
    override val applyPlatformSystemWindowFitting = false

    private var isLoading by mutableStateOf(false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.activationComposeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        binding.activationComposeView.setContent {
            EltaTheme {
                ActivationScreen(
                    isLoading = isLoading,
                    onBack = { router.exit() },
                    onResend = {
                        if (!isLoading) presentationModel.sendAgainAction.consumer.accept(Unit)
                    },
                    onCheckActivation = {
                        if (!isLoading) presentationModel.continueAction.consumer.accept(Unit)
                    }
                )
            }
        }
    }

    override fun onBindPresentationModel(pm: ActivationPm) {
        super.onBindPresentationModel(pm)
        pm.progressState.bindTo { isLoading = it }
    }

    companion object {
        fun newInstance(): ActivationFragment = ActivationFragment()
    }
}
