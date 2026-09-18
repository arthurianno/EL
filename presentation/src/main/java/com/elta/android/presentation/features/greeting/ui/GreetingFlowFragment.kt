package com.elta.android.presentation.features.greeting.ui

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.TransparentLightStatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentGreetingBinding
import com.elta.android.presentation.features.greeting.model.GreetingState
import com.elta.android.presentation.features.greeting.pm.GreetingPm
import com.elta.android.presentation.theme.EltaTheme
import me.dmdev.rxpm.bindTo

class GreetingFlowFragment :
    BaseFragment<GreetingPm, FragmentGreetingBinding>(FragmentGreetingBinding::inflate) {

    override val screenLayout: Int = R.layout.fragment_greeting
    override val classToken: Class<GreetingPm> = GreetingPm::class.java
    override val statusBarConfigProvider = TransparentLightStatusBarConfigProvider
    override val applyBottomSystemInsets = false
    override val applyPlatformSystemWindowFitting = false

    private var uiState by mutableStateOf(GreetingState())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.greetingComposeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        binding.greetingComposeView.setContent {
            EltaTheme {
                GreetingScreen(
                    state = uiState,
                    onLogin = { presentationModel.menuAction.consumer.accept(Unit) },
                    onRegistration = { presentationModel.registrationAction.consumer.accept(Unit) }
                )
            }
        }
    }

    override fun onBindPresentationModel(pm: GreetingPm) {
        super.onBindPresentationModel(pm)
        pm.screenConfigState.bindTo { uiState = uiState.copy(screenConfig = it) }
    }

    companion object {
        fun newInstance(): GreetingFlowFragment = GreetingFlowFragment()
    }
}
