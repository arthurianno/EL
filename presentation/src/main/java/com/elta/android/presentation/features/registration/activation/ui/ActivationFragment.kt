package com.elta.android.presentation.features.registration.activation.ui

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.features.registration.activation.pm.ActivationPm
import com.jakewharton.rxbinding2.view.clicks
import kotlinx.android.synthetic.main.layout_auth_toolbar.*
import kotlinx.android.synthetic.main.layout_state.*

class ActivationFragment : BaseFragment<ActivationPm>() {

    override val screenLayout: Int = R.layout.fragment_activate_profile
    override val classToken: Class<ActivationPm> = ActivationPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        menuButtonView.setText(R.string.registration_send_again)
    }

    override fun onBindPresentationModel(pm: ActivationPm) {
        super.onBindPresentationModel(pm)
        menuButtonView.clicks().bindTo(pm.sendAgainAction)
        stateButtonView.clicks().bindTo(pm.continueAction)
        bindProgressDialog(pm)
    }

    companion object {
        fun newInstance(): ActivationFragment = ActivationFragment()
    }
}
