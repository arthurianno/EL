package com.elta.android.presentation.features.greeting.ui

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFlowFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.features.greeting.pm.GreetingPm
import com.jakewharton.rxbinding2.view.clicks
import com.nullgr.core.ui.extensions.hide
import kotlinx.android.synthetic.main.fragment_greeting.*
import kotlinx.android.synthetic.main.layout_auth_toolbar.*

class GreetingFlowFragment : BaseFlowFragment<GreetingPm>() {

    override val screenLayout: Int = R.layout.fragment_greeting
    override val classToken: Class<GreetingPm> = GreetingPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeButtonView.hide()
        menuButtonView.setText(R.string.greeting_menu_action)
    }

    override fun onBindPresentationModel(pm: GreetingPm) {
        super.onBindPresentationModel(pm)
        menuButtonView.clicks().bindTo(pm.menuAction)
        registrationButtonView.clicks().bindTo(pm.registrationAction)
    }

    companion object {
        fun newInstance(): GreetingFlowFragment = GreetingFlowFragment()
    }
}
