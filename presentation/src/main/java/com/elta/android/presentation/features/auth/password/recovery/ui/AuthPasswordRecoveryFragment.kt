package com.elta.android.presentation.features.auth.password.recovery.ui

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.features.auth.password.recovery.pm.AuthPasswordRecoveryPm
import com.elta.android.presentation.utils.error
import com.jakewharton.rxbinding2.view.clicks
import kotlinx.android.synthetic.main.fragment_auth_password_recovery.*
import kotlinx.android.synthetic.main.layout_toolbar.*

class AuthPasswordRecoveryFragment : BaseFragment<AuthPasswordRecoveryPm>() {

    override val screenLayout: Int = R.layout.fragment_auth_password_recovery
    override val classToken: Class<AuthPasswordRecoveryPm> = AuthPasswordRecoveryPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeButtonView.setImageResource(R.drawable.ic_dialog_close)
    }

    override fun onBindPresentationModel(pm: AuthPasswordRecoveryPm) {
        super.onBindPresentationModel(pm)
        pm.emailInput.bindTo(emailInputView)
        pm.emailInput.error.observable
            .distinctUntilChanged()
            .bindTo(emailInputView.error())
        pm.continueEnabledState.bindTo { sendLinkButtonView.isEnabled = it }
        sendLinkButtonView.clicks().bindTo(pm.continueAction)
        bindProgressDialog(pm)
    }

    companion object {
        fun newInstance() = AuthPasswordRecoveryFragment()
    }
}
