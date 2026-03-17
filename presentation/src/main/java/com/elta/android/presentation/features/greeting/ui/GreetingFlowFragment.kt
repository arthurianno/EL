package com.elta.android.presentation.features.greeting.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import coil.load
import coil.request.CachePolicy
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentGreetingBinding
import com.elta.android.presentation.features.greeting.pm.GreetingPm
import com.jakewharton.rxbinding2.view.clicks
import com.nullgr.core.ui.extensions.hide
import me.dmdev.rxpm.bindTo
import java.util.Locale

class GreetingFlowFragment :
    BaseFragment<GreetingPm, FragmentGreetingBinding>(FragmentGreetingBinding::inflate) {
    companion object {
        private const val TAG = "LangFlow"

        fun newInstance(): GreetingFlowFragment = GreetingFlowFragment()
    }

    override val screenLayout: Int = R.layout.fragment_greeting
    override val classToken: Class<GreetingPm> = GreetingPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.homeButtonView.hide()
        binding.toolbar.menuButtonView.setText(R.string.greeting_menu_action)
        Log.i(
            TAG,
            "GreetingFlowFragment.onViewCreated(locale=${Locale.getDefault().language}, menu='${binding.toolbar.menuButtonView.text}')"
        )
    }

    override fun onBindPresentationModel(pm: GreetingPm) {
        super.onBindPresentationModel(pm)
        binding.toolbar.menuButtonView.clicks().bindTo(pm.menuAction)
        binding.registrationButtonView.clicks().bindTo(pm.registrationAction)
        bindScreenConfig(pm) {
            withBackgroundImage(binding.backgroundImageView, R.drawable.ic_welcome)
            withTitle(binding.greetingTitleView, R.string.greeting_title)
            withDescription(binding.greetingMessageView, R.string.greeting_message)
            withRootView(binding.root)
            onConfigLoaded { screenEntity ->
                Log.i(
                    TAG,
                    "Greeting config applied(lang=${screenEntity?.lang}, title='${screenEntity?.title}', description='${screenEntity?.description}')"
                )
            }
        }

    }
}
